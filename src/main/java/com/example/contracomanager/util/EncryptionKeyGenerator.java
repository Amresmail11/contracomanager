package com.example.contracomanager.util;

import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionKeyGenerator {
    public static void main(String[] args) {
        // Generate a 256-bit (32-byte) key
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[32];
        secureRandom.nextBytes(key);
        
        // Encode it in Base64
        String encodedKey = Base64.getEncoder().encodeToString(key);
        System.out.println("Generated Encryption Key: " + encodedKey);
    }
} 