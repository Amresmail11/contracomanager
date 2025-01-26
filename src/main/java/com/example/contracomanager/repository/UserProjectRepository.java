package com.example.contracomanager.repository;

import com.example.contracomanager.model.UserProject;
import com.example.contracomanager.model.UserProjectId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserProjectRepository extends JpaRepository<UserProject, UserProjectId> {
    boolean existsByUserIdAndProjectId(UUID userId, UUID projectId);
    
    @EntityGraph(attributePaths = {"user"}, type = EntityGraph.EntityGraphType.FETCH)
    List<UserProject> findAllByUserId(UUID userId);
    
    @EntityGraph(attributePaths = {"user"}, type = EntityGraph.EntityGraphType.FETCH)
    List<UserProject> findAllByProjectId(UUID projectId);
    
    @EntityGraph(attributePaths = {"user"}, type = EntityGraph.EntityGraphType.FETCH)
    List<UserProject> findAllByProjectIdAndRole(UUID projectId, String role);
    
    boolean existsByUserIdAndProjectIdAndRole(UUID userId, UUID projectId, String role);
} 