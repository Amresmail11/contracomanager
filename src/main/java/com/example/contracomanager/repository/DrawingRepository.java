package com.example.contracomanager.repository;

import com.example.contracomanager.model.Drawing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DrawingRepository extends JpaRepository<Drawing, UUID> {
    List<Drawing> findByProjectId(UUID projectId);
    List<Drawing> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
    boolean existsByFileUrl(String fileUrl);
} 