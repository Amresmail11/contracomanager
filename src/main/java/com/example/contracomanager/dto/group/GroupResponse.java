package com.example.contracomanager.dto.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponse {
    private UUID id;
    private String name;
    private UUID projectId;
    private String projectName;
    private UUID createdById;
    private String createdByName;
    private ZonedDateTime createdAt;
    private List<GroupMemberResponse> members;
} 