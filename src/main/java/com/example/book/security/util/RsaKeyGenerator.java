package com.example.book.security.util;

import java.security.KeyPair;
import java.util.Base64;

/**
 * Utility class để generate RSA key pair và xuất ra Base64
 * <p>
 * Sử dụng để tạo keys cho application.yaml
 */
public class RsaKeyGenerator {

    public static void main(String[] args) {
        try {
            // Generate RSA key pair
            KeyPair keyPair = RsaUtil.generateKeyPair();

            // Convert to Base64
            String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

            System.out.println("=== RSA Public Key (Base64) ===");
            System.out.println(publicKeyBase64);
            System.out.println("\n=== RSA Private Key (Base64) ===");
            System.out.println(privateKeyBase64);
            System.out.println("\n=== Copy vào application.yaml ===");
            System.out.println("encryption:");
            System.out.println("  rsa:");
            System.out.println("    public-key: " + publicKeyBase64);
            System.out.println("    private-key: " + privateKeyBase64);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
