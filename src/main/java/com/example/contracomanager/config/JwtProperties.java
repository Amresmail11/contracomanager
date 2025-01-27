package com.example.contracomanager.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;
import java.util.Base64;

@Configuration
@ConfigurationProperties(prefix = "app.security.jwt")
@Data
public class JwtProperties {
    private String secret;
    private long expiration = 86400000; // 24 hours in milliseconds

    @PostConstruct
    public void init() {
        if (secret == null || secret.equals("your-256-bit-secret")) {
            // Generate a 512-bit (64-byte) key for JWT
            SecureRandom secureRandom = new SecureRandom();
            byte[] keyBytes = new byte[64];
            secureRandom.nextBytes(keyBytes);
            
            // Encode it in Base64
            this.secret = Base64.getEncoder().encodeToString(keyBytes);
        }
    }
} 