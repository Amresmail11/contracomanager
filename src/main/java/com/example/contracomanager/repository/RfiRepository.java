package com.example.contracomanager.repository;

import com.example.contracomanager.model.Rfi;
import com.example.contracomanager.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RfiRepository extends JpaRepository<Rfi, UUID> {
    @EntityGraph(attributePaths = {"project", "createdBy", "assignedToUser", "assignedToGroup", "assignedToGroup.members", "replies", "replies.createdBy"}, type = EntityGraph.EntityGraphType.LOAD)
    Page<Rfi> findByProjectId(UUID projectId, Pageable pageable);

    @EntityGraph(attributePaths = {"project", "createdBy", "assignedToUser", "assignedToGroup", "assignedToGroup.members", "replies", "replies.createdBy"}, type = EntityGraph.EntityGraphType.LOAD)
    Page<Rfi> findByProjectIdAndStatus(UUID projectId, String status, Pageable pageable);

    @EntityGraph(attributePaths = {"project", "createdBy", "assignedToUser", "assignedToGroup", "assignedToGroup.members", "replies", "replies.createdBy"}, type = EntityGraph.EntityGraphType.LOAD)
    Page<Rfi> findByAssignedToUserAndAssignedType(User user, String assignedType, Pageable pageable);

    @EntityGraph(attributePaths = {"project", "createdBy", "assignedToUser", "assignedToGroup", "assignedToGroup.members", "replies", "replies.createdBy"}, type = EntityGraph.EntityGraphType.LOAD)
    Page<Rfi> findByCreatedBy(User user, Pageable pageable);

    List<Rfi> findByDueDateBefore(LocalDateTime date);
    List<Rfi> findByProjectIdAndDueDateBefore(UUID projectId, LocalDateTime date);
} 