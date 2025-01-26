package com.example.contracomanager.repository;

import com.example.contracomanager.model.UserProject;
import com.example.contracomanager.model.UserProjectId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserProjectRepository extends JpaRepository<UserProject, UserProjectId> {
    boolean existsByUserIdAndProjectId(UUID userId, UUID projectId);
    List<UserProject> findAllByUserId(UUID userId);
    List<UserProject> findAllByProjectId(UUID projectId);
    List<UserProject> findAllByProjectIdAndRole(UUID projectId, String role);
    boolean existsByUserIdAndProjectIdAndRole(UUID userId, UUID projectId, String role);
} 