package com.example.book.security.util;

import com.example.book.exception.CryptoException;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class cho AES/GCM (Symmetric Encryption)
 *
 * - Encrypt dữ liệu thật
 * - Đảm bảo confidentiality + integrity
 */
public final class AesUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;   // 96-bit
    private static final int GCM_TAG_LENGTH = 128; // auth tag

    private AesUtil() {}

    /**
     * Encrypt plaintext bằng AES/GCM
     */
    public static String encrypt(String plainText, SecretKey key) {
        try {
            byte[] iv = SecureRandom.getInstanceStrong()
                    .generateSeed(GCM_IV_LENGTH);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    key,
                    new GCMParameterSpec(GCM_TAG_LENGTH, iv)
            );

            byte[] cipherText = cipher.doFinal(
                    plainText.getBytes(StandardCharsets.UTF_8)
            );

            byte[] combined = ByteBuffer
                    .allocate(iv.length + cipherText.length)
                    .put(iv)
                    .put(cipherText)
                    .array();

            return Base64.getEncoder().encodeToString(combined);

        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("AES/GCM algorithm not supported by JVM", e);

        } catch (GeneralSecurityException e) {
            throw new CryptoException("AES/GCM encryption failed", e);
        }
    }

    /**
     * Decrypt AES/GCM ciphertext
     */
    public static String decrypt(String cipherText, SecretKey key) {
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherText);

            if (decoded.length < GCM_IV_LENGTH + 1) {
                throw new CryptoException("Ciphertext too short");
            }

            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);

            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    key,
                    new GCMParameterSpec(GCM_TAG_LENGTH, iv)
            );

            byte[] plainBytes = cipher.doFinal(encrypted);
            return new String(plainBytes, StandardCharsets.UTF_8);

        } catch (IllegalArgumentException e) {
            throw new CryptoException("Invalid Base64 ciphertext format", e);

        } catch (AEADBadTagException e) {
            throw new CryptoException("Invalid AES key or tampered ciphertext", e);

        } catch (GeneralSecurityException e) {
            throw new CryptoException("AES/GCM decryption failed", e);
        }
    }

    /**
     * Generate AES 256-bit key
     */
    public static SecretKey generateKey() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(KEY_SIZE);
            return generator.generateKey();

        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Generate AES key failed", e);
        }
    }

    /**
     * Convert Base64 string → AES SecretKey
     */
    public static SecretKey keyFromString(String keyString) {
        byte[] decoded = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(decoded, "AES");
    }

    /**
     * Convert AES SecretKey → Base64 string
     */
    public static String keyToString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}
