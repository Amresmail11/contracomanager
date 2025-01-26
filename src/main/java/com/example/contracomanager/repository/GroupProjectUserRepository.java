package com.example.contracomanager.repository;

import com.example.contracomanager.model.GroupProjectUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupProjectUserRepository extends JpaRepository<GroupProjectUser, UUID> {
    List<GroupProjectUser> findAllByProjectCode(String projectCode);
    List<GroupProjectUser> findAllByGroupId(UUID groupId);
    List<GroupProjectUser> findAllByUserId(UUID userId);
    void deleteAllByGroupId(UUID groupId);
    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);
} 