package com.example.contracomanager.dto.group;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddGroupMembersRequest {
    @NotEmpty(message = "Member usernames list cannot be empty")
    @Size(min = 1, message = "At least one member username must be provided")
    private List<String> memberUsernames;
} 