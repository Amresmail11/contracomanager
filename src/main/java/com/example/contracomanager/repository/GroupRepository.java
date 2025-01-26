package com.example.contracomanager.repository;

import com.example.contracomanager.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
    List<Group> findByProjectId(UUID projectId);
    List<Group> findByProjectIdAndCreatedById(UUID projectId, UUID userId);
    boolean existsByIdAndCreatedById(UUID groupId, UUID userId);
    Optional<Group> findByNameAndProjectId(String name, UUID projectId);
    @Query("SELECT g FROM Group g WHERE g.name = :name AND g.project.code = :projectCode")
    Optional<Group> findByNameAndProjectCode(@Param("name") String name, @Param("projectCode") String projectCode);
} 