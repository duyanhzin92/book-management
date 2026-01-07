package com.example.book.security.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility class cho mã hóa AES (Symmetric Encryption)
 * <p>
 * AES dùng để mã hóa dữ liệu thật như:
 * <ul>
 *     <li>Encrypt CIF</li>
 *     <li>Encrypt số thẻ</li>
 *     <li>Encrypt thông tin nhạy cảm trong DB</li>
 * </ul>
 * <p>
 * Đặc điểm:
 * <ul>
 *     <li>1 key duy nhất (symmetric)</li>
 *     <li>Tốc độ rất nhanh</li>
 *     <li>Phải bảo vệ key cẩn thận</li>
 * </ul>
 */
public class AesUtil {

    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 256; // Ngân hàng thường dùng 256-bit

    /**
     * Mã hóa plaintext bằng AES
     *
     * @param plainText văn bản gốc cần mã hóa
     * @param key       secret key để mã hóa
     * @return chuỗi đã mã hóa (Base64 encoded)
     * @throws Exception nếu có lỗi trong quá trình mã hóa
     */
    public static String encrypt(String plainText, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * Giải mã ciphertext bằng AES
     *
     * @param cipherText chuỗi đã mã hóa (Base64 encoded)
     * @param key        secret key để giải mã
     * @return văn bản gốc đã được giải mã
     * @throws Exception nếu có lỗi trong quá trình giải mã
     */
    public static String decrypt(String cipherText, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decoded = Base64.getDecoder().decode(cipherText);
        return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
    }

    /**
     * Sinh AES key mới (256-bit)
     *
     * @return SecretKey mới
     * @throws NoSuchAlgorithmException nếu thuật toán không được hỗ trợ
     */
    public static SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEY_SIZE);
        return keyGenerator.generateKey();
    }

    /**
     * Chuyển đổi từ Base64 string sang SecretKey
     *
     * @param keyString chuỗi key đã encode Base64
     * @return SecretKey
     */
    public static SecretKey keyFromString(String keyString) {
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
    }

    /**
     * Chuyển đổi SecretKey sang Base64 string để lưu trữ
     *
     * @param key SecretKey
     * @return chuỗi Base64
     */
    public static String keyToString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}



