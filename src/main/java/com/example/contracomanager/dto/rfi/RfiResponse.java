package com.example.contracomanager.dto.rfi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RfiResponse {
    private UUID id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String projectCode;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private Map<String, Object> createdBy;
    private Map<String, Object> resolvedBy;
    private String assignedType;
    private String assignedTo;  // This will contain either username or group name
    private List<RfiReplyResponse> replies;
} 