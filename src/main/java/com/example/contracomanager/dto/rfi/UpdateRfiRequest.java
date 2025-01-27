package com.example.contracomanager.dto.rfi;

import com.example.contracomanager.model.RfiPriority;
import com.example.contracomanager.model.RfiStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRfiRequest {
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    private RfiPriority priority;
    private RfiStatus status;
    private LocalDateTime dueDate;

    @Email(message = "Must be a valid email address")
    private String assignedToEmail;
} 