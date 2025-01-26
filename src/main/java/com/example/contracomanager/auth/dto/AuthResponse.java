package com.example.contracomanager.auth.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class AuthResponse {
    private String status;
    private String message;
    private String jwtToken;
    private Map<String, Object> user;
} 