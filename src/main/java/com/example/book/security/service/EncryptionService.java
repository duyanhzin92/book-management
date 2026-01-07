package com.example.book.security.service;

import com.example.book.security.util.AesUtil;
import com.example.book.security.util.RsaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Service để xử lý mã hóa AES và RSA
 * <p>
 * Sử dụng keys từ application.yaml
 */
@Service
@Slf4j
public class EncryptionService {

    private final SecretKey aesKey;
    private final PublicKey rsaPublicKey;
    private final PrivateKey rsaPrivateKey;

    public EncryptionService(
            @Value("${encryption.aes.key}") String aesKeyBase64,
            @Value("${encryption.rsa.public-key:}") String rsaPublicKeyBase64,
            @Value("${encryption.rsa.private-key:}") String rsaPrivateKeyBase64) {
        this.aesKey = AesUtil.keyFromString(aesKeyBase64);
        
        // RSA keys optional - nếu chưa có thì generate tạm
        if (rsaPublicKeyBase64 != null && !rsaPublicKeyBase64.isEmpty() 
            && rsaPrivateKeyBase64 != null && !rsaPrivateKeyBase64.isEmpty()) {
            this.rsaPublicKey = parseRSAPublicKey(rsaPublicKeyBase64);
            this.rsaPrivateKey = parseRSAPrivateKey(rsaPrivateKeyBase64);
        } else {
            // Generate temporary RSA keys
            log.warn("RSA keys not found in config, generating temporary keys. " +
                    "Run RsaKeyGenerator to generate real keys!");
            try {
                java.security.KeyPair keyPair = RsaUtil.generateKeyPair();
                this.rsaPublicKey = keyPair.getPublic();
                this.rsaPrivateKey = keyPair.getPrivate();
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize RSA keys", e);
            }
        }
        
        log.info("EncryptionService initialized with AES key and RSA keys");
    }

    // ============ AES Methods ============

    /**
     * Mã hóa dữ liệu bằng AES
     *
     * @param plainText văn bản gốc cần mã hóa
     * @return chuỗi đã mã hóa (Base64 encoded)
     */
    public String encryptAES(String plainText) {
        try {
            return AesUtil.encrypt(plainText, aesKey);
        } catch (Exception e) {
            log.error("Error encrypting with AES", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Giải mã dữ liệu bằng AES
     *
     * @param cipherText chuỗi đã mã hóa (Base64 encoded)
     * @return văn bản gốc đã được giải mã
     */
    public String decryptAES(String cipherText) {
        try {
            return AesUtil.decrypt(cipherText, aesKey);
        } catch (Exception e) {
            log.error("Error decrypting with AES", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    // ============ RSA Methods ============

    /**
     * Mã hóa dữ liệu bằng RSA Public Key
     * <p>
     * Lưu ý: Chỉ dùng để mã hóa key nhỏ (như AES key), không dùng mã hóa dữ liệu lớn
     *
     * @param data dữ liệu cần mã hóa
     * @return chuỗi đã mã hóa (Base64 encoded)
     */
    public String encryptRSA(String data) {
        try {
            return RsaUtil.encrypt(data, rsaPublicKey);
        } catch (Exception e) {
            log.error("Error encrypting with RSA", e);
            throw new RuntimeException("RSA encryption failed", e);
        }
    }

    /**
     * Giải mã dữ liệu bằng RSA Private Key
     *
     * @param data chuỗi đã mã hóa (Base64 encoded)
     * @return dữ liệu gốc đã được giải mã
     */
    public String decryptRSA(String data) {
        try {
            return RsaUtil.decrypt(data, rsaPrivateKey);
        } catch (Exception e) {
            log.error("Error decrypting with RSA", e);
            throw new RuntimeException("RSA decryption failed", e);
        }
    }

    /**
     * Mã hóa AES key bằng RSA (hybrid encryption)
     * <p>
     * Luồng: Client gửi AES key đã mã hóa bằng RSA public key,
     * Server giải mã bằng RSA private key để lấy AES key
     *
     * @param aesKeyBase64 AES key dạng Base64
     * @return AES key đã mã hóa bằng RSA
     */
    public String encryptAESKeyWithRSA(String aesKeyBase64) {
        return encryptRSA(aesKeyBase64);
    }

    /**
     * Giải mã AES key bằng RSA private key
     *
     * @param encryptedAESKey AES key đã mã hóa bằng RSA
     * @return AES key dạng Base64
     */
    public String decryptAESKeyWithRSA(String encryptedAESKey) {
        return decryptRSA(encryptedAESKey);
    }

    // ============ Helper Methods ============

    /**
     * Parse RSA Public Key từ Base64 string
     */
    private PublicKey parseRSAPublicKey(String base64Key) {
        try {
            byte[] keyBytes = java.util.Base64.getDecoder().decode(base64Key);
            java.security.spec.X509EncodedKeySpec spec = new java.security.spec.X509EncodedKeySpec(keyBytes);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            log.error("Error parsing RSA public key", e);
            throw new RuntimeException("Failed to parse RSA public key", e);
        }
    }

    /**
     * Parse RSA Private Key từ Base64 string
     */
    private PrivateKey parseRSAPrivateKey(String base64Key) {
        try {
            byte[] keyBytes = java.util.Base64.getDecoder().decode(base64Key);
            java.security.spec.PKCS8EncodedKeySpec spec = new java.security.spec.PKCS8EncodedKeySpec(keyBytes);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            log.error("Error parsing RSA private key", e);
            throw new RuntimeException("Failed to parse RSA private key", e);
        }
    }

    /**
     * Get RSA Public Key dạng Base64 (để gửi cho client)
     *
     * @return RSA Public Key Base64 string
     */
    public String getRSAPublicKeyBase64() {
        return java.util.Base64.getEncoder().encodeToString(rsaPublicKey.getEncoded());
    }
}
