package com.example.contracomanager.service;

import com.example.contracomanager.model.Project;
import com.example.contracomanager.model.User;
import com.example.contracomanager.repository.ProjectRepository;
import com.example.contracomanager.repository.UserRepository;
import com.example.contracomanager.repository.UserProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserProjectRepository userProjectRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, UserProjectRepository userProjectRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.userProjectRepository = userProjectRepository;
    }

    public Optional<Project> findByCode(String code) {
        return projectRepository.findByCode(code);
    }

    public List<User> getProjectUsers(String code) {
        return projectRepository.findByCode(code)
            .map(project -> userRepository.findAllByProjectId(project.getId()))
            .orElse(List.of());
    }

    public boolean hasAccess(String code, UUID userId) {
        Optional<Project> project = projectRepository.findByCode(code);
        if (project.isEmpty()) {
            return false;
        }
        
        // Check if user exists in user_project table for this project with either ADMIN or MEMBER role
        return userProjectRepository.existsByUserIdAndProjectIdAndRole(userId, project.get().getId(), "ADMIN") ||
               userProjectRepository.existsByUserIdAndProjectIdAndRole(userId, project.get().getId(), "MEMBER");
    }
} 