package com.example.contracomanager.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;
import java.util.Base64;

@Configuration
@ConfigurationProperties(prefix = "app.encryption")
@Data
public class EncryptionProperties {
    private String key;

    @PostConstruct
    public void init() {
        if (key == null || key.equals("your-encryption-key")) {
            // Generate a 256-bit (32-byte) key
            SecureRandom secureRandom = new SecureRandom();
            byte[] keyBytes = new byte[32];
            secureRandom.nextBytes(keyBytes);
            
            // Encode it in Base64
            this.key = Base64.getEncoder().encodeToString(keyBytes);
        }
    }
} 