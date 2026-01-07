package com.example.book.security.service;

import org.springframework.stereotype.Service;

/**
 * Service để xử lý logic matching URL với pattern
 * <p>
 * Hỗ trợ các loại pattern:
 * <ul>
 *     <li>Exact match: /api/books</li>
 *     <li>Wildcard: /api/books/**</li>
 *     <li>Path variable: /api/books/{id}</li>
 * </ul>
 */
@Service
public class UrlMatcherService {

    /**
     * Kiểm tra URL có match với pattern không
     * <p>
     * Hỗ trợ:
     * <ul>
     *     <li>Exact match: /api/books</li>
     *     <li>Wildcard: /api/books/**</li>
     *     <li>Path variable: /api/books/{id}</li>
     * </ul>
     *
     * @param requestUrl URL từ request (ví dụ: /api/books/1)
     * @param pattern    pattern từ permission (ví dụ: /api/books/{id}, /api/books/**)
     * @return true nếu match
     */
    public boolean matchesUrl(String requestUrl, String pattern) {
        // Exact match
        if (requestUrl.equals(pattern)) {
            return true;
        }

        // Wildcard pattern: /api/books/** matches /api/books, /api/books/1, /api/books/1/comments
        if (pattern.endsWith("/**")) {
            String basePattern = pattern.substring(0, pattern.length() - 3);
            return requestUrl.startsWith(basePattern);
        }

        // Path variable pattern: /api/books/{id} matches /api/books/1, /api/books/123
        String regexPattern = pattern.replaceAll("\\{[^}]+\\}", "[^/]+");
        return requestUrl.matches(regexPattern);
    }

    /**
     * Kiểm tra permission có match với URL và method không
     *
     * @param permissionUrl    URL pattern từ permission
     * @param permissionMethod HTTP method từ permission
     * @param requestUrl      URL từ request
     * @param requestMethod   HTTP method từ request
     * @return true nếu match
     */
    public boolean matchesPermission(String permissionUrl, String permissionMethod,
                                     String requestUrl, String requestMethod) {
        return matchesUrl(requestUrl, permissionUrl) 
                && permissionMethod.equalsIgnoreCase(requestMethod);
    }
}
