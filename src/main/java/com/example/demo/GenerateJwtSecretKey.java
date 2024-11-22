package com.example.demo;

import java.util.Base64;
import java.security.SecureRandom;

public class GenerateJwtSecretKey {
    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        byte[] secretKeyBytes = new byte[32]; // 256 bits = 32 bytes
        random.nextBytes(secretKeyBytes);

        // Base64 encode the key
        String encodedSecretKey = Base64.getEncoder().encodeToString(secretKeyBytes);
        System.out.println("Base64-encoded secret key: " + encodedSecretKey);
    }
}

