package com.example.book.security.filter;

import com.example.book.security.jwt.JwtUtil;
import com.example.book.security.service.PermissionService;
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

        } catch (Exception e) {
            log.error("JWT authentication failed", e);
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"Access denied\"}");
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



