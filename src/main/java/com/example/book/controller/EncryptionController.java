package com.example.book.controller;

import com.example.book.dto.response.ApiResponse;
import com.example.book.security.service.EncryptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller để test và demo các chức năng mã hóa AES và RSA
 */
@RestController
@RequestMapping("/api/encryption")
@RequiredArgsConstructor
@Tag(name = "Encryption", description = "APIs để test mã hóa AES và RSA")
public class EncryptionController {

    private final EncryptionService encryptionService;

    /**
     * Mã hóa dữ liệu bằng AES
     */
    @PostMapping("/aes/encrypt")
    @Operation(summary = "Mã hóa AES", description = "Mã hóa plaintext bằng AES-256")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Mã hóa thành công"
            )
    })
    public ResponseEntity<ApiResponse<EncryptResponse>> encryptAES(@RequestBody EncryptRequest request) {
        String encrypted = encryptionService.encryptAES(request.getPlainText());
        return ResponseEntity.ok(ApiResponse.success("Mã hóa thành công", 
                new EncryptResponse(encrypted)));
    }

    /**
     * Giải mã dữ liệu bằng AES
     */
    @PostMapping("/aes/decrypt")
    @Operation(summary = "Giải mã AES", description = "Giải mã ciphertext bằng AES-256")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Giải mã thành công"
            )
    })
    public ResponseEntity<ApiResponse<DecryptResponse>> decryptAES(@RequestBody DecryptRequest request) {
        String decrypted = encryptionService.decryptAES(request.getCipherText());
        return ResponseEntity.ok(ApiResponse.success("Giải mã thành công", 
                new DecryptResponse(decrypted)));
    }

    /**
     * Mã hóa dữ liệu bằng RSA
     */
    @PostMapping("/rsa/encrypt")
    @Operation(summary = "Mã hóa RSA", description = "Mã hóa dữ liệu bằng RSA public key (chỉ dùng cho dữ liệu nhỏ như keys)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Mã hóa thành công"
            )
    })
    public ResponseEntity<ApiResponse<EncryptResponse>> encryptRSA(@RequestBody EncryptRequest request) {
        String encrypted = encryptionService.encryptRSA(request.getPlainText());
        return ResponseEntity.ok(ApiResponse.success("Mã hóa RSA thành công", 
                new EncryptResponse(encrypted)));
    }

    /**
     * Giải mã dữ liệu bằng RSA
     */
    @PostMapping("/rsa/decrypt")
    @Operation(summary = "Giải mã RSA", description = "Giải mã dữ liệu bằng RSA private key")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Giải mã thành công"
            )
    })
    public ResponseEntity<ApiResponse<DecryptResponse>> decryptRSA(@RequestBody DecryptRequest request) {
        String decrypted = encryptionService.decryptRSA(request.getCipherText());
        return ResponseEntity.ok(ApiResponse.success("Giải mã RSA thành công", 
                new DecryptResponse(decrypted)));
    }

    /**
     * Lấy RSA Public Key (để client có thể mã hóa AES key)
     */
    @GetMapping("/rsa/public-key")
    @Operation(summary = "Lấy RSA Public Key", description = "Lấy RSA Public Key dạng Base64 để client có thể mã hóa AES key")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lấy public key thành công"
            )
    })
    public ResponseEntity<ApiResponse<PublicKeyResponse>> getRSAPublicKey() {
        String publicKey = encryptionService.getRSAPublicKeyBase64();
        return ResponseEntity.ok(ApiResponse.success("Lấy public key thành công", 
                new PublicKeyResponse(publicKey)));
    }

    /**
     * Hybrid Encryption: Mã hóa AES key bằng RSA
     */
    @PostMapping("/hybrid/encrypt-aes-key")
    @Operation(summary = "Mã hóa AES key bằng RSA", description = "Hybrid encryption: Mã hóa AES key bằng RSA public key")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Mã hóa AES key thành công"
            )
    })
    public ResponseEntity<ApiResponse<EncryptResponse>> encryptAESKeyWithRSA(@RequestBody EncryptRequest request) {
        String encrypted = encryptionService.encryptAESKeyWithRSA(request.getPlainText());
        return ResponseEntity.ok(ApiResponse.success("Mã hóa AES key bằng RSA thành công", 
                new EncryptResponse(encrypted)));
    }

    // ============ DTOs ============

    @Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EncryptRequest {
        private String plainText;
    }

    @Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DecryptRequest {
        private String cipherText;
    }

    @Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EncryptResponse {
        private String encrypted;
    }

    @Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DecryptResponse {
        private String decrypted;
    }

    @Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PublicKeyResponse {
        private String publicKey;
    }
}
