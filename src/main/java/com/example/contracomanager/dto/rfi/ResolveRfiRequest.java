package com.example.contracomanager.dto.rfi;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveRfiRequest {
    @NotBlank(message = "Resolution message is required")
    private String message;
} 