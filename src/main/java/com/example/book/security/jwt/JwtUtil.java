package com.example.book.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class cho JWT (JSON Web Token)
 * <p>
 * JWT payload CHỈ chứa những thông tin cần thiết:
 * <ul>
 *     <li>userId</li>
 *     <li>role</li>
 * </ul>
 * <p>
 * ❌ KHÔNG chứa:
 * <ul>
 *     <li>Permission list (load từ DB khi cần)</li>
 *     <li>Thông tin nhạy cảm</li>
 * </ul>
 * <p>
 * Signature sử dụng SHA256withRSA (không phải để mã hóa, mà để ký)
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret:your-256-bit-secret-key-for-hmac-sha256-algorithm-minimum-32-characters}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 hours default
    private Long expiration;

    /**
     * Sinh SecretKey từ secret string
     *
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Tạo JWT token từ userId và role
     *
     * @param userId ID của user
     * @param role   role của user
     * @return JWT token string
     */
    public String generateToken(Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        return createToken(claims, userId.toString());
    }

    /**
     * Tạo JWT token với claims
     *
     * @param claims thông tin cần đưa vào token
     * @param subject subject của token (thường là userId)
     * @return JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Lấy userId từ token
     *
     * @param token JWT token
     * @return userId
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * Lấy role từ token
     *
     * @param token JWT token
     * @return role name
     */
    public String getRoleFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return (String) claims.get("role");
    }

    /**
     * Lấy expiration date từ token
     *
     * @param token JWT token
     * @return expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Lấy claim cụ thể từ token
     *
     * @param token          JWT token
     * @param claimsResolver function để extract claim
     * @param <T>            type của claim
     * @return claim value
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Lấy tất cả claims từ token
     *
     * @param token JWT token
     * @return Claims object
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Kiểm tra token có hết hạn không
     *
     * @param token JWT token
     * @return true nếu token chưa hết hạn
     */
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Validate token
     * <p>
     * Kiểm tra:
     * <ul>
     *     <li>Token có đúng format không</li>
     *     <li>Signature có hợp lệ không</li>
     *     <li>Token có hết hạn không</li>
     * </ul>
     *
     * @param token JWT token
     * @return true nếu token hợp lệ, false nếu không hợp lệ
     */
    public Boolean validateToken(String token) {
        try {
            // Parse và verify token (sẽ throw exception nếu invalid)
            getAllClaimsFromToken(token);
            
            // Kiểm tra expiration
            boolean expired = isTokenExpired(token);
            if (expired) {
                log.debug("Token expired");
                return false;
            }
            
            log.debug("Token validated successfully");
            return true;
        } catch (ExpiredJwtException e) {
            // Token đã hết hạn
            log.debug("Token expired: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            // JWT malformed, invalid format, ...
            log.warn("JWT validation failed: {}", e.getMessage());
            log.debug("JwtException details:", e);
            return false;
        } catch (IllegalArgumentException e) {
            // Token null hoặc empty
            log.debug("Invalid token argument: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            /**
             * Fallback cuối cùng cho các exception chưa được handle ở trên.
             * <p>
             * Các exception đã được handle:
             * <ul>
             *     <li>ExpiredJwtException - Token expired</li>
             *     <li>JwtException - JWT validation failed (signature invalid, malformed, ...)</li>
             *     <li>IllegalArgumentException - Invalid argument</li>
             * </ul>
             * <p>
             * Nếu exception rơi vào đây, có thể là:
             * <ul>
             *     <li>NullPointerException - token null</li>
             *     <li>RuntimeException khác - logic error</li>
             *     <li>Unexpected checked exception - cần thêm handler cụ thể</li>
             * </ul>
             * <p>
             * ⚠️ Phải log đầy đủ để debug!
             */
            log.error("Unexpected error validating token: {}", e.getMessage(), e);
            log.error("Exception type: {}", e.getClass().getName());
            log.debug("Exception stacktrace:", e);
            return false;
        }
    }
}



