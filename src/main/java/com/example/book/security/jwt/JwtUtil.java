package com.example.book.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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
     *
     * @param token JWT token
     * @return true nếu token hợp lệ
     */
    public Boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}



