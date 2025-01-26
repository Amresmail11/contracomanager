package com.example.contracomanager.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProjectId implements Serializable {
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(name = "project_id")
    private UUID projectId;
} 