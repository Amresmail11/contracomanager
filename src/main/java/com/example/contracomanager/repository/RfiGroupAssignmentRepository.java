package com.example.contracomanager.repository;

import com.example.contracomanager.model.RfiGroupAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RfiGroupAssignmentRepository extends JpaRepository<RfiGroupAssignment, UUID> {
    List<RfiGroupAssignment> findAllByRfiId(UUID rfiId);
    List<RfiGroupAssignment> findAllByUserId(UUID userId);
    List<RfiGroupAssignment> findAllByGroupId(UUID groupId);
    void deleteAllByRfiId(UUID rfiId);
    boolean existsByRfiIdAndUserId(UUID rfiId, UUID userId);
} 