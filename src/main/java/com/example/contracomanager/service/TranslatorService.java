package com.example.contracomanager.service;

import com.example.contracomanager.model.Group;
import com.example.contracomanager.model.Project;
import com.example.contracomanager.model.User;
import com.example.contracomanager.repository.GroupRepository;
import com.example.contracomanager.repository.ProjectRepository;
import com.example.contracomanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TranslatorService {
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ProjectRepository projectRepository;

    /**
     * Get user ID by username
     */
    public Optional<UUID> getUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(User::getId);
    }

    /**
     * Get user by username
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Get group ID by name and project
     */
    public Optional<UUID> getGroupIdByNameAndProject(String groupName, UUID projectId) {
        return groupRepository.findByNameAndProjectId(groupName, projectId)
            .map(Group::getId);
    }

    /**
     * Get group by name and project
     */
    public Optional<Group> getGroupByNameAndProject(String groupName, UUID projectId) {
        return groupRepository.findByNameAndProjectId(groupName, projectId);
    }

    /**
     * Get project ID by code
     */
    public Optional<UUID> getProjectIdByCode(String projectCode) {
        return projectRepository.findByCode(projectCode)
            .map(Project::getId);
    }

    /**
     * Get project by code
     */
    public Optional<Project> getProjectByCode(String projectCode) {
        return projectRepository.findByCode(projectCode);
    }

    /**
     * Translate username to ID, throwing exception if not found
     */
    public UUID translateUserUsername(String username) {
        return getUserIdByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
    }

    /**
     * Translate group name to ID within a project context, throwing exception if not found
     */
    public UUID translateGroupName(String groupName, UUID projectId) {
        return getGroupIdByNameAndProject(groupName, projectId)
            .orElseThrow(() -> new IllegalArgumentException("Group not found with name: " + groupName));
    }

    /**
     * Translate project code to ID, throwing exception if not found
     */
    public UUID translateProjectCode(String projectCode) {
        return getProjectIdByCode(projectCode)
            .orElseThrow(() -> new IllegalArgumentException("Project not found with code: " + projectCode));
    }
} 