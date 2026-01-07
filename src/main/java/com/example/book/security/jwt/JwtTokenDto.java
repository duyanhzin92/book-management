package com.example.book.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO chứa thông tin JWT token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtTokenDto {
    private String token;

    /**
     * Loại token, mặc định là Bearer
     */
    @Builder.Default
    private String type = "Bearer";

    private Long userId;
    private String role;
}
