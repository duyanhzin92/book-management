package com.example.book.security.util;

import com.example.book.exception.CryptoException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

/**
 * Utility class cho RSA (Asymmetric Encryption)
 *
 * ⚠️ RSA KHÔNG dùng để mã hóa dữ liệu lớn
 * - Chỉ dùng để encrypt AES key / small secret
 */
public final class RsaUtil {

    private static final String ALGORITHM = "RSA/ECB/PKCS1Padding";
    private static final int KEY_SIZE = 2048;

    private RsaUtil() {}

    /**
     * Encrypt dữ liệu nhỏ bằng RSA Public Key
     */
    public static String encrypt(String data, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] encrypted = cipher.doFinal(
                    data.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getEncoder().encodeToString(encrypted);

        } catch (InvalidKeyException e) {
            throw new CryptoException("Invalid RSA public key", e);

        } catch (IllegalBlockSizeException e) {
            throw new CryptoException("Data too large for RSA encryption", e);

        } catch (GeneralSecurityException e) {
            throw new CryptoException("RSA encryption failed", e);
        }
    }

    /**
     * Decrypt dữ liệu bằng RSA Private Key
     */
    public static String decrypt(String data, PrivateKey privateKey) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(data);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (IllegalArgumentException e) {
            throw new CryptoException("Invalid Base64 ciphertext", e);

        } catch (BadPaddingException e) {
            throw new CryptoException("Invalid RSA private key or corrupted ciphertext", e);

        } catch (GeneralSecurityException e) {
            throw new CryptoException("RSA decryption failed", e);
        }
    }

    /**
     * Generate RSA KeyPair (2048-bit)
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(KEY_SIZE);
            return generator.generateKeyPair();

        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Generate RSA key pair failed", e);
        }
    }
}
