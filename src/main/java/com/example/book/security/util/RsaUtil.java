package com.example.book.security.util;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import javax.crypto.Cipher;

/**
 * Utility class cho mã hóa RSA (Asymmetric Encryption)
 * <p>
 * RSA KHÔNG dùng để mã hóa dữ liệu, mà dùng để:
 * <ul>
 *     <li>Trao đổi key (encrypt AES key)</li>
 *     <li>Ký số (digital signature)</li>
 *     <li>TLS handshake</li>
 * </ul>
 * <p>
 * Đặc điểm:
 * <ul>
 *     <li>Public key + Private key (asymmetric)</li>
 *     <li>Tốc độ rất chậm (không phù hợp mã hóa dữ liệu lớn)</li>
 *     <li>Key size ≥ 2048 (bắt buộc)</li>
 * </ul>
 */
public class RsaUtil {

    private static final String ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048; // Ngân hàng bắt buộc ≥ 2048

    /**
     * Mã hóa dữ liệu bằng RSA Public Key
     * <p>
     * Lưu ý: Chỉ dùng để mã hóa key nhỏ (như AES key), không dùng mã hóa dữ liệu lớn
     *
     * @param data      dữ liệu cần mã hóa (thường là AES key)
     * @param publicKey public key để mã hóa
     * @return chuỗi đã mã hóa (Base64 encoded)
     * @throws Exception nếu có lỗi trong quá trình mã hóa
     */
    public static String encrypt(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * Giải mã dữ liệu bằng RSA Private Key
     *
     * @param data       chuỗi đã mã hóa (Base64 encoded)
     * @param privateKey private key để giải mã
     * @return dữ liệu gốc đã được giải mã
     * @throws Exception nếu có lỗi trong quá trình giải mã
     */
    public static String decrypt(String data, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] decoded = Base64.getDecoder().decode(data);
        return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
    }

    /**
     * Sinh RSA KeyPair mới (2048-bit)
     *
     * @return KeyPair chứa PublicKey và PrivateKey
     * @throws Exception nếu có lỗi trong quá trình sinh key
     */
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
        generator.initialize(KEY_SIZE);
        return generator.generateKeyPair();
    }
}



