package com.example.book.security.util;

import com.example.book.exception.CryptoException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256; // Ngân hàng thường dùng 256-bit
    private static final int GCM_IV_LENGTH = 12;   // 96 bit
    private static final int GCM_TAG_LENGTH = 128; // auth tag

    /**
     * Mã hóa plaintext bằng AES/GCM.
     *
     * - Sinh IV ngẫu nhiên cho mỗi lần mã hóa (96-bit theo khuyến nghị NIST)
     * - Sử dụng AES/GCM để vừa mã hóa vừa đảm bảo toàn vẹn dữ liệu
     * - Kết quả trả về là Base64(IV + cipherText)
     */
    public static String encrypt(String plainText, SecretKey key) {
        try {
            // Sinh IV ngẫu nhiên cho AES/GCM (không được reuse)
            byte[] iv = SecureRandom.getInstanceStrong().generateSeed(GCM_IV_LENGTH);

            // Khởi tạo Cipher với AES/GCM/NoPadding
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            // GCMParameterSpec chứa độ dài auth tag và IV
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            // Thực hiện mã hóa plaintext
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Ghép IV + ciphertext để phục vụ giải mã sau này
            byte[] combined = ByteBuffer.allocate(iv.length + encrypted.length)
                    .put(iv)
                    .put(encrypted)
                    .array();

            // Encode Base64 để dễ lưu trữ / truyền qua network
            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception ex) {
            // Wrap mọi lỗi crypto thành exception nghiệp vụ
            throw new CryptoException("AES encrypt failed", ex);
        }
    }



    /**
     * Giải mã ciphertext đã được mã hóa bằng AES/GCM.
     *
     * - Tách IV từ dữ liệu đầu vào
     * - Dùng lại IV để khởi tạo Cipher
     * - Nếu dữ liệu bị sửa hoặc key sai -> GCM sẽ tự động detect và throw exception
     */
    public static String decrypt(String cipherText, SecretKey key) {
        try {
            // Decode Base64 để lấy dữ liệu gốc (IV + ciphertext)
            byte[] decoded = Base64.getDecoder().decode(cipherText);

            // Dùng ByteBuffer để tách IV và phần dữ liệu đã mã hóa
            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            // Đọc IV (96-bit)
            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);

            // Phần còn lại là ciphertext + auth tag
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            // Khởi tạo Cipher để giải mã
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            // Thực hiện giải mã
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);

        } catch (Exception ex) {
            // Sai key / dữ liệu bị sửa / lỗi crypto đều đi vào đây
            throw new CryptoException("AES decrypt failed", ex);
        }
    }


    /**
     * Sinh khóa AES mới với độ dài 256-bit.
     *
     * - Khóa dùng cho mã hóa dữ liệu nhạy cảm
     * - Phải được lưu trữ và bảo vệ an toàn (vault, env, HSM...)
     */
    public static SecretKey generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(KEY_SIZE);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException ex) {
            // Lỗi hạ tầng: JVM không hỗ trợ AES
            throw new CryptoException("Generate AES key failed", ex);
        }
    }



    /**
     * Chuyển Base64 string sang SecretKey AES.
     *
     * - Dùng khi load key từ config / DB / secret manager
     */
    public static SecretKey keyFromString(String keyString) {
        // Decode Base64 để lấy raw key bytes
        byte[] decodedKey = Base64.getDecoder().decode(keyString);

        // Tạo SecretKeySpec cho thuật toán AES
        return new SecretKeySpec(decodedKey, "AES");
    }


    /**
     * Chuyển SecretKey AES sang Base64 string để lưu trữ.
     *
     * - Không lưu key dưới dạng plain text
     */
    public static String keyToString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

}



