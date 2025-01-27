package com.example.contracomanager.dto.rfi;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRfiByNameRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Priority is required")
    private String priority;

    @NotNull(message = "Project code is required")
    private String projectCode;

    private LocalDate deadline;

    @Email(message = "Must be a valid email address")
    private String assignedToEmail;
    
    private String assignedGroupName;
} 