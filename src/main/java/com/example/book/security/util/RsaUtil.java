package com.example.book.security.util;

import com.example.book.exception.CryptoException;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Utility class dùng cho mã hóa RSA (Asymmetric Encryption)
 *
 * ❗ RSA KHÔNG dùng để mã hóa dữ liệu lớn
 * → Chỉ dùng cho các mục đích:
 *   - Mã hóa khóa đối xứng (AES key)
 *   - Ký số (Digital Signature)
 *   - Trao đổi khóa trong TLS/HTTPS
 *
 * Đặc điểm RSA:
 * - Dùng cặp khóa: Public Key + Private Key
 * - Public Key: dùng để mã hóa
 * - Private Key: dùng để giải mã
 * - Tốc độ chậm, chi phí tính toán cao
 * - Key size tối thiểu: 2048 bit (bắt buộc trong thực tế)
 */
public class RsaUtil {

    /**
     * Transformation của RSA:
     * - RSA      : thuật toán bất đối xứng
     * - ECB      : mode (bắt buộc với RSA, không giống AES)
     * - PKCS1Padding : padding tiêu chuẩn cho RSA encryption
     *
     * ⚠️ RSA KHÔNG dùng IV nên không có GCMParameterSpec như AES
     */
    private static final String ALGORITHM = "RSA/ECB/PKCS1Padding";

    /**
     * Độ dài key RSA (bit)
     * 2048 bit là mức tối thiểu an toàn hiện nay
     */
    private static final int KEY_SIZE = 2048;

    /**
     * Mã hóa dữ liệu bằng RSA Public Key
     *
     * 👉 Thường dùng để:
     * - Encrypt AES key trước khi gửi qua network
     *
     * ❌ Không dùng để encrypt:
     * - JSON
     * - Password dài
     * - File
     *
     * @param data dữ liệu cần mã hóa (thường là AES key)
     * @param publicKey public key dùng để mã hóa
     * @return chuỗi Base64 chứa dữ liệu đã mã hóa
     */
    public static String encrypt(String data, PublicKey publicKey) {
        try {
            // Tạo instance Cipher theo transformation RSA/ECB/PKCS1Padding
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            // Khởi tạo Cipher ở chế độ ENCRYPT với Public Key
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            // Chuyển dữ liệu String → byte[] (UTF-8)
            byte[] inputBytes = data.getBytes(StandardCharsets.UTF_8);

            // Thực hiện mã hóa
            byte[] encryptedBytes = cipher.doFinal(inputBytes);

            // Encode sang Base64 để dễ lưu DB / truyền HTTP
            return Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (NoSuchAlgorithmException |
                 NoSuchPaddingException |
                 InvalidKeyException |
                 BadPaddingException |
                 IllegalBlockSizeException ex) {

            // Bọc exception crypto thành custom exception
            throw new CryptoException("Encrypt data failed", ex);
        }
    }

    /**
     * Giải mã dữ liệu bằng RSA Private Key
     *
     * 👉 Dùng ở phía server để:
     * - Giải mã AES key đã được encrypt bằng Public Key
     *
     * @param data chuỗi Base64 đã mã hóa
     * @param privateKey private key dùng để giải mã
     * @return dữ liệu gốc sau khi giải mã
     */
    public static String decrypt(String data, PrivateKey privateKey) {
        try {
            // Tạo instance Cipher với cùng transformation
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            // Khởi tạo Cipher ở chế độ DECRYPT với Private Key
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            // Decode Base64 → byte[]
            byte[] encryptedBytes = Base64.getDecoder().decode(data);

            // Thực hiện giải mã
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            // Chuyển byte[] → String UTF-8
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (NoSuchAlgorithmException |
                 NoSuchPaddingException |
                 InvalidKeyException |
                 BadPaddingException |
                 IllegalBlockSizeException |
                 IllegalArgumentException ex) {

            // IllegalArgumentException có thể xảy ra khi Base64 sai
            throw new CryptoException("Decrypt data failed", ex);
        }
    }

    /**
     * Sinh mới một cặp RSA KeyPair (Public + Private)
     *
     * 👉 Thường dùng khi:
     * - Khởi tạo hệ thống
     * - Sinh key cho client / service
     *
     * @return KeyPair gồm PublicKey và PrivateKey
     */
    public static KeyPair generateKeyPair() {
        try {
            // Tạo KeyPairGenerator cho thuật toán RSA
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

            // Cấu hình độ dài key (2048 bit)
            generator.initialize(KEY_SIZE);

            // Sinh cặp key
            return generator.generateKeyPair();

        } catch (NoSuchAlgorithmException ex) {
            throw new CryptoException("Generate RSA key pair failed", ex);
        }
    }

}
