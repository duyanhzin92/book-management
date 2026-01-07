package com.example.book.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request đăng nhập với hybrid encryption (AES + RSA)
 * <p>
 * Flow:
 * <ol>
 *     <li>Client generate AES key (256-bit)</li>
 *     <li>AES encrypt password → encryptedPassword</li>
 *     <li>RSA encrypt AES key → encryptedAesKey</li>
 *     <li>Gửi lên server: username + encryptedPassword + encryptedAesKey</li>
 * </ol>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username không được để trống")
    private String username;

    /**
     * Mật khẩu đã được AES encrypt, Base64 encoded
     */
    @NotBlank(message = "encryptedPassword không được để trống")
    private String encryptedPassword;

    /**
     * AES key (Base64) đã được RSA encrypt, Base64 encoded
     */
    @NotBlank(message = "encryptedAesKey không được để trống")
    private String encryptedAesKey;
}



