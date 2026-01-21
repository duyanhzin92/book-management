package com.example.book.security.filter;

import com.example.book.security.jwt.JwtUtil;
import com.example.book.security.service.PermissionService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT Filter để xác thực và kiểm tra permission
 * <p>
 * Luồng xử lý:
 * <ol>
 *     <li>Extract JWT token từ request header</li>
 *     <li>Validate token</li>
 *     <li>Decode token để lấy userId và role</li>
 *     <li>Load permissions từ Enum (RoleEnum) - không hard-code</li>
 *     <li>Match URL + METHOD với permissions</li>
 *     <li>OK → set authentication, FAIL → 403</li>
 * </ol>
 * <p>
 * Tất cả role và permission được định nghĩa trong Enum,
 * không hard-code trong filter này
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_NAME = "Authorization";

    private final JwtUtil jwtUtil;
    private final PermissionService permissionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);

            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                // Lấy userId và role từ token
                Long userId = jwtUtil.getUserIdFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                // Kiểm tra permission
                String requestUrl = request.getRequestURI();
                String requestMethod = request.getMethod();

                // Load permissions từ Enum và check
                boolean hasPermission = permissionService.hasPermission(role, requestUrl, requestMethod);

                if (!hasPermission) {
                    log.warn("Access denied for user {} with role {} to {} {}", userId, role, requestMethod, requestUrl);
                    throw new AccessDeniedException("Permission denied");
                }

                // Set authentication context
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Authenticated user {} with role {} for {} {}", userId, role, requestMethod, requestUrl);
            }

        } catch (AccessDeniedException e) {
            // Permission denied - đã log ở trên
            log.warn("Access denied: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            try {
                response.getWriter().write("{\"error\":\"Access denied\"}");
            } catch (IOException ioException) {
                log.error("Failed to write error response", ioException);
            }
            return;
        } catch (ExpiredJwtException e) {
            // Token đã hết hạn
            log.warn("JWT token expired: {}", e.getMessage());
            log.debug("ExpiredJwtException details:", e);
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            try {
                response.getWriter().write("{\"error\":\"Token expired\"}");
            } catch (IOException ioException) {
                log.error("Failed to write error response", ioException);
            }
            return;
        } catch (JwtException e) {
            // JWT signature invalid, malformed, invalid format, ...
            log.warn("JWT validation failed: {}", e.getMessage());
            log.debug("JwtException details:", e);
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            try {
                response.getWriter().write("{\"error\":\"Invalid token\"}");
            } catch (IOException ioException) {
                log.error("Failed to write error response", ioException);
            }
            return;
        } catch (IllegalArgumentException e) {
            // Invalid token format, null token, ...
            log.warn("Invalid JWT token format: {}", e.getMessage());
            log.debug("IllegalArgumentException details:", e);
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            try {
                response.getWriter().write("{\"error\":\"Invalid token format\"}");
            } catch (IOException ioException) {
                log.error("Failed to write error response", ioException);
            }
            return;
        } catch (Exception e) {
            /**
             * Fallback cuối cùng cho các exception chưa được handle ở trên.
             * <p>
             * Các exception đã được handle:
             * <ul>
             *     <li>AccessDeniedException - Permission denied</li>
             *     <li>ExpiredJwtException - Token expired</li>
             *     <li>JwtException - JWT validation failed (signature invalid, malformed, ...)</li>
             *     <li>IllegalArgumentException - Invalid token format</li>
             * </ul>
             * <p>
             * Lưu ý: IOException từ response.getWriter().write() đã được handle trong các inner try-catch blocks.
             * IOException từ filterChain.doFilter() sẽ được propagate lên (method signature throws IOException).
             * <p>
             * Nếu exception rơi vào đây, có thể là:
             * <ul>
             *     <li>NullPointerException - null check thiếu</li>
             *     <li>RuntimeException khác - logic error</li>
             *     <li>Unexpected checked exception - cần thêm handler cụ thể</li>
             * </ul>
             * <p>
             * ⚠️ Phải log đầy đủ để debug và fix sau này!
             */
            log.error("Unexpected error in JWT authentication filter: {}", e.getMessage(), e);
            log.error("Exception type: {}", e.getClass().getName());
            log.error("Exception stacktrace:", e);
            if (e.getCause() != null) {
                log.error("Caused by: {} - {}", e.getCause().getClass().getName(), e.getCause().getMessage());
            }
            
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            try {
                response.getWriter().write("{\"error\":\"Authentication failed\"}");
            } catch (IOException ioException) {
                log.error("Failed to write error response after unexpected exception", ioException);
            }
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token từ request header
     *
     * @param request HTTP request
     * @return JWT token hoặc null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_NAME);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}



