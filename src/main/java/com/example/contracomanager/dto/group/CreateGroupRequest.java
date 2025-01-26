package com.example.contracomanager.dto.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupRequest {
    @NotBlank(message = "Group name is required")
    @Size(min = 3, max = 50, message = "Group name must be between 3 and 50 characters")
    private String name;

    @NotBlank(message = "Project code is required")
    private String projectCode;

    @NotEmpty(message = "Member usernames are required")
    private List<String> memberUsernames;
} 