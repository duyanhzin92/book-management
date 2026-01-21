package com.example.book.security.service;

import com.example.book.exception.CryptoException;
import com.example.book.security.util.AesUtil;
import com.example.book.security.util.RsaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * Service để xử lý mã hóa AES và RSA
 * <p>
 * Sử dụng keys từ application.yaml
 * <p>
 * Flow mã hóa hybrid (AES + RSA):
 * <ol>
 *     <li>Client: Generate AES key → AES encrypt password → RSA encrypt AES key</li>
 *     <li>Server: RSA decrypt AES key → AES decrypt password</li>
 * </ol>
 */
@Service
@Slf4j
public class EncryptionService {

    private final SecretKey aesKey;
    private final PublicKey rsaPublicKey;
    private final PrivateKey rsaPrivateKey;

    /**
     * Constructor để khởi tạo EncryptionService với keys từ config
     *
     * @param aesKeyBase64        AES key dạng Base64 (bắt buộc)
     * @param rsaPublicKeyBase64  RSA public key dạng Base64 (optional)
     * @param rsaPrivateKeyBase64 RSA private key dạng Base64 (optional)
     */
    public EncryptionService(
            @Value("${encryption.aes.key}") String aesKeyBase64,
            @Value("${encryption.rsa.public-key:}") String rsaPublicKeyBase64,
            @Value("${encryption.rsa.private-key:}") String rsaPrivateKeyBase64) {
        
        // Load AES key (bắt buộc)
        try {
            this.aesKey = AesUtil.keyFromString(aesKeyBase64);
            log.info("AES key loaded successfully from config");
        } catch (IllegalArgumentException e) {
            log.error("Failed to load AES key from config: invalid Base64 format", e);
            throw new CryptoException("Invalid AES key format in config", e);
        }
        
        // RSA keys optional - nếu chưa có thì generate tạm
        // Khởi tạo RSA keys: thử load từ config, nếu fail thì generate tạm
        PublicKey tempPublicKey;
        PrivateKey tempPrivateKey;
        
        if (rsaPublicKeyBase64 != null && !rsaPublicKeyBase64.isEmpty() 
            && rsaPrivateKeyBase64 != null && !rsaPrivateKeyBase64.isEmpty()) {
            try {
                // Thử parse RSA keys từ config
                tempPublicKey = parseRSAPublicKey(rsaPublicKeyBase64);
                tempPrivateKey = parseRSAPrivateKey(rsaPrivateKeyBase64);
                log.info("RSA keys loaded successfully from config");
            } catch (CryptoException e) {
                log.error("Failed to parse RSA keys from config, will generate temporary keys", e);
                // Fallback: generate temporary keys
                KeyPair tempKeyPair = generateTemporaryRSAKeys();
                tempPublicKey = tempKeyPair.getPublic();
                tempPrivateKey = tempKeyPair.getPrivate();
            }
        } else {
            // Generate temporary RSA keys
            log.warn("RSA keys not found in config, generating temporary keys. " +
                    "Run RsaKeyGenerator to generate real keys for production!");
            KeyPair tempKeyPair = generateTemporaryRSAKeys();
            tempPublicKey = tempKeyPair.getPublic();
            tempPrivateKey = tempKeyPair.getPrivate();
        }
        
        // Gán vào final fields (đảm bảo luôn được khởi tạo trong mọi code path)
        // Các biến temp đã được khởi tạo trong mọi code path ở trên
        this.rsaPublicKey = tempPublicKey;
        this.rsaPrivateKey = tempPrivateKey;
        
        log.info("EncryptionService initialized successfully");
    }

    // ============ AES Methods ============

    /**
     * Mã hóa dữ liệu bằng AES-256/GCM
     * <p>
     * Sử dụng AES key từ config (server-side encryption)
     *
     * @param plainText văn bản gốc cần mã hóa
     * @return chuỗi đã mã hóa (Base64 encoded)
     * @throws CryptoException nếu có lỗi trong quá trình mã hóa
     */
    public String encryptAES(String plainText) {
        try {
            log.debug("Encrypting data with AES (length: {})", plainText != null ? plainText.length() : 0);
            String encrypted = AesUtil.encrypt(plainText, aesKey);
            log.debug("AES encryption completed successfully");
            return encrypted;
        } catch (CryptoException e) {
            log.error("AES encryption failed: {}", e.getMessage(), e);
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("AES encryption failed: invalid input data", e);
            throw new CryptoException("Invalid input data for AES encryption", e);
        }
    }

    /**
     * Giải mã dữ liệu bằng AES-256/GCM
     * <p>
     * Sử dụng AES key từ config (server-side decryption)
     *
     * @param cipherText chuỗi đã mã hóa (Base64 encoded)
     * @return văn bản gốc đã được giải mã
     * @throws CryptoException nếu có lỗi trong quá trình giải mã (key sai, data bị tamper, ...)
     */
    public String decryptAES(String cipherText) {
        try {
            log.debug("Decrypting data with AES (cipherText length: {})", cipherText != null ? cipherText.length() : 0);
            String decrypted = AesUtil.decrypt(cipherText, aesKey);
            log.debug("AES decryption completed successfully");
            return decrypted;
        } catch (CryptoException e) {
            log.error("AES decryption failed: {}", e.getMessage(), e);
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("AES decryption failed: invalid Base64 ciphertext", e);
            throw new CryptoException("Invalid Base64 ciphertext format", e);
        }
    }

    // ============ RSA Methods ============

    /**
     * Mã hóa dữ liệu bằng RSA Public Key
     * <p>
     * Lưu ý: Chỉ dùng để mã hóa key nhỏ (như AES key), không dùng mã hóa dữ liệu lớn
     * <p>
     * RSA có giới hạn kích thước dữ liệu:
     * - 2048-bit key: max ~245 bytes
     * - Nếu dữ liệu lớn hơn sẽ throw exception
     *
     * @param data dữ liệu cần mã hóa (thường là AES key Base64)
     * @return chuỗi đã mã hóa (Base64 encoded)
     * @throws CryptoException nếu có lỗi trong quá trình mã hóa (data quá lớn, key invalid, ...)
     */
    public String encryptRSA(String data) {
        try {
            log.debug("Encrypting data with RSA (data length: {})", data != null ? data.length() : 0);
            String encrypted = RsaUtil.encrypt(data, rsaPublicKey);
            log.debug("RSA encryption completed successfully");
            return encrypted;
        } catch (CryptoException e) {
            log.error("RSA encryption failed: {}", e.getMessage(), e);
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("RSA encryption failed: invalid input data", e);
            throw new CryptoException("Invalid input data for RSA encryption", e);
        }
    }

    /**
     * Giải mã dữ liệu bằng RSA Private Key
     * <p>
     * Dùng để giải mã dữ liệu đã được mã hóa bằng RSA public key
     *
     * @param data chuỗi đã mã hóa (Base64 encoded)
     * @return dữ liệu gốc đã được giải mã
     * @throws CryptoException nếu có lỗi trong quá trình giải mã (key sai, data bị tamper, ...)
     */
    public String decryptRSA(String data) {
        try {
            log.debug("Decrypting data with RSA (data length: {})", data != null ? data.length() : 0);
            String decrypted = RsaUtil.decrypt(data, rsaPrivateKey);
            log.debug("RSA decryption completed successfully");
            return decrypted;
        } catch (CryptoException e) {
            log.error("RSA decryption failed: {}", e.getMessage(), e);
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("RSA decryption failed: invalid Base64 ciphertext", e);
            throw new CryptoException("Invalid Base64 ciphertext format", e);
        }
    }

    /**
     * Mã hóa AES key bằng RSA (hybrid encryption)
     * <p>
     * Luồng: Client gửi AES key đã mã hóa bằng RSA public key,
     * Server giải mã bằng RSA private key để lấy AES key
     * <p>
     * Đây là pattern chuẩn trong banking:
     * - Client: Generate AES key → RSA encrypt AES key → Gửi lên server
     * - Server: RSA decrypt AES key → Dùng AES key để decrypt password
     *
     * @param aesKeyBase64 AES key dạng Base64
     * @return AES key đã mã hóa bằng RSA (Base64)
     * @throws CryptoException nếu AES key quá lớn hoặc RSA encryption failed
     */
    public String encryptAESKeyWithRSA(String aesKeyBase64) {
        log.debug("Encrypting AES key with RSA (key length: {})", aesKeyBase64 != null ? aesKeyBase64.length() : 0);
        return encryptRSA(aesKeyBase64);
    }

    /**
     * Giải mã AES key bằng RSA private key
     * <p>
     * Dùng trong flow login: Client gửi encryptedAesKey, server giải mã để lấy AES key
     *
     * @param encryptedAESKey AES key đã mã hóa bằng RSA (Base64)
     * @return AES key dạng Base64
     * @throws CryptoException nếu RSA decryption failed hoặc key không hợp lệ
     */
    public String decryptAESKeyWithRSA(String encryptedAESKey) {
        log.debug("Decrypting AES key with RSA (encrypted key length: {})", encryptedAESKey != null ? encryptedAESKey.length() : 0);
        return decryptRSA(encryptedAESKey);
    }

    /**
     * Giải mã password từ payload client (hybrid AES + RSA)
     * <p>
     * Client flow:
     * <ol>
     *     <li>Generate AES key (256-bit, random)</li>
     *     <li>encryptedPassword = AES(password, aesKey)</li>
     *     <li>encryptedAesKey = RSA(aesKeyBase64, serverPublicKey)</li>
     * </ol>
     * Server flow (method này):
     * <ol>
     *     <li>aesKeyBase64 = RSA decrypt(encryptedAesKey, serverPrivateKey)</li>
     *     <li>aesKey = AesUtil.keyFromString(aesKeyBase64)</li>
     *     <li>password = AES decrypt(encryptedPassword, aesKey)</li>
     * </ol>
     *
     * @param encryptedPassword mật khẩu đã mã hóa bằng AES (Base64)
     * @param encryptedAesKey   AES key (Base64) đã mã hóa bằng RSA (Base64)
     * @return mật khẩu gốc (plaintext)
     * @throws CryptoException nếu giải mã thất bại (key sai, data bị tamper, format sai, ...)
     */
    public String decryptClientPassword(String encryptedPassword, String encryptedAesKey) {
        log.debug("Decrypting client password (hybrid AES+RSA)");
        
        try {
            // Step 1: Giải mã AES key bằng RSA private key → aesKeyBase64
            log.debug("Step 1: Decrypting AES key with RSA private key");
            String aesKeyBase64;
            try {
                aesKeyBase64 = decryptAESKeyWithRSA(encryptedAesKey);
            } catch (CryptoException e) {
                log.error("Failed to decrypt AES key with RSA: {}", e.getMessage(), e);
                throw new CryptoException("Invalid encrypted AES key (RSA decryption failed)", e);
            }

            // Step 2: Convert Base64 → SecretKey
            log.debug("Step 2: Converting Base64 AES key to SecretKey");
            SecretKey dynamicAesKey;
            try {
                dynamicAesKey = AesUtil.keyFromString(aesKeyBase64);
            } catch (IllegalArgumentException e) {
                log.error("Failed to parse AES key from Base64: {}", e.getMessage(), e);
                throw new CryptoException("Invalid AES key format (not valid Base64)", e);
            }

            // Step 3: Dùng AES key này để giải mã password
            log.debug("Step 3: Decrypting password with AES key");
            try {
                String password = AesUtil.decrypt(encryptedPassword, dynamicAesKey);
                log.debug("Client password decrypted successfully");
                return password;
            } catch (CryptoException e) {
                log.error("Failed to decrypt password with AES: {}", e.getMessage(), e);
                throw new CryptoException("Invalid encrypted password (AES decryption failed - wrong key or tampered data)", e);
            }
            
        } catch (CryptoException e) {
            // Re-throw CryptoException as-is (đã log ở trên)
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("Invalid input format for decryptClientPassword: {}", e.getMessage(), e);
            throw new CryptoException("Invalid input format (Base64 decode failed)", e);
        }
    }

    // ============ Helper Methods ============

    /**
     * Parse RSA Public Key từ Base64 string
     * <p>
     * Format: X.509 encoded public key (Base64)
     *
     * @param base64Key RSA public key dạng Base64
     * @return PublicKey object
     * @throws CryptoException nếu parse failed (invalid format, invalid key spec, ...)
     */
    private PublicKey parseRSAPublicKey(String base64Key) {
        try {
            log.debug("Parsing RSA public key from Base64");
            byte[] keyBytes = java.util.Base64.getDecoder().decode(base64Key);
            java.security.spec.X509EncodedKeySpec spec = new java.security.spec.X509EncodedKeySpec(keyBytes);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(spec);
            log.debug("RSA public key parsed successfully");
            return publicKey;
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse RSA public key: invalid Base64 format", e);
            throw new CryptoException("Invalid Base64 format for RSA public key", e);
        } catch (InvalidKeySpecException e) {
            log.error("Failed to parse RSA public key: invalid key specification", e);
            throw new CryptoException("Invalid RSA public key specification", e);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to parse RSA public key: RSA algorithm not supported", e);
            throw new CryptoException("RSA algorithm not supported by JVM", e);
        }
    }

    /**
     * Parse RSA Private Key từ Base64 string
     * <p>
     * Format: PKCS#8 encoded private key (Base64)
     *
     * @param base64Key RSA private key dạng Base64
     * @return PrivateKey object
     * @throws CryptoException nếu parse failed (invalid format, invalid key spec, ...)
     */
    private PrivateKey parseRSAPrivateKey(String base64Key) {
        try {
            log.debug("Parsing RSA private key from Base64");
            byte[] keyBytes = java.util.Base64.getDecoder().decode(base64Key);
            java.security.spec.PKCS8EncodedKeySpec spec = new java.security.spec.PKCS8EncodedKeySpec(keyBytes);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(spec);
            log.debug("RSA private key parsed successfully");
            return privateKey;
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse RSA private key: invalid Base64 format", e);
            throw new CryptoException("Invalid Base64 format for RSA private key", e);
        } catch (InvalidKeySpecException e) {
            log.error("Failed to parse RSA private key: invalid key specification", e);
            throw new CryptoException("Invalid RSA private key specification", e);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to parse RSA private key: RSA algorithm not supported", e);
            throw new CryptoException("RSA algorithm not supported by JVM", e);
        }
    }

    /**
     * Generate temporary RSA key pair (dùng khi không có keys trong config)
     * <p>
     * ⚠️ Chỉ dùng cho development/testing, không dùng trong production!
     *
     * @return KeyPair chứa PublicKey và PrivateKey
     * @throws CryptoException nếu generate failed
     */
    private KeyPair generateTemporaryRSAKeys() {
        try {
            log.warn("Generating temporary RSA key pair (2048-bit)");
            KeyPair keyPair = RsaUtil.generateKeyPair();
            log.warn("Temporary RSA key pair generated successfully");
            return keyPair;
        } catch (CryptoException e) {
            log.error("Failed to generate temporary RSA keys: {}", e.getMessage(), e);
            throw new CryptoException("Failed to generate temporary RSA keys", e);
        }
    }

    /**
     * Get RSA Public Key dạng Base64 (để gửi cho client)
     * <p>
     * Client cần public key này để mã hóa AES key trước khi gửi lên server
     *
     * @return RSA Public Key Base64 string
     */
    public String getRSAPublicKeyBase64() {
        return java.util.Base64.getEncoder().encodeToString(rsaPublicKey.getEncoded());
    }
}
