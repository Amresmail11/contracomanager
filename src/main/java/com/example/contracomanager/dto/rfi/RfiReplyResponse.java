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
public class RfiReplyResponse {
    private UUID id;
    private String content;
    private LocalDateTime createdAt;
    private Map<String, Object> createdBy;
} 