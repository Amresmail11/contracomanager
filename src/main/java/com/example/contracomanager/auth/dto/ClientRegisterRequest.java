package com.example.contracomanager.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientRegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @NotBlank(message = "Display name is required")
    private String displayName;
    
    @NotBlank(message = "Project code is required")
    private String projectCode;
    
    @NotBlank(message = "Project name is required")
    private String projectName;
    
    @NotBlank(message = "Job is required")
    private String job;
} 