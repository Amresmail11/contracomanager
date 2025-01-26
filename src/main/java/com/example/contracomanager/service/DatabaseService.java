package com.example.contracomanager.service;

import com.example.contracomanager.dto.auth.ClientRegisterRequest;
import com.example.contracomanager.dto.auth.RegisterRequest;
import com.example.contracomanager.model.Project;
import com.example.contracomanager.model.User;
import com.example.contracomanager.model.UserRole;
import com.example.contracomanager.model.UserProject;
import com.example.contracomanager.model.UserProjectId;
import com.example.contracomanager.repository.ProjectRepository;
import com.example.contracomanager.repository.UserRepository;
import com.example.contracomanager.repository.UserProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DatabaseService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> getUserByUid(UUID userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Transactional
    public Project saveProject(Project project) {
        if (project.getCode() == null || project.getCode().trim().isEmpty()) {
            project.setCode(generateUniqueProjectCode());
        }
        return projectRepository.save(project);
    }

    public Optional<Project> getProjectById(UUID projectId) {
        return projectRepository.findById(projectId);
    }

    public Optional<Project> getProjectByCode(String code) {
        return projectRepository.findByCode(code);
    }

    @Transactional
    public User createUser(RegisterRequest request, UUID uid) {
        String projectCode = generateUniqueProjectCode();
        
        User newUser = User.builder()
            .id(uid)
            .email(request.getEmail())
            .username(request.getUsername())
            .job(request.getJob())
            .role(UserRole.ADMIN)
            .createdAt(ZonedDateTime.now())
            .build();

        // Create a new project for the user
        Project project = Project.builder()
            .name(request.getUsername() + "'s Project")
            .code(projectCode)
            .createdBy(newUser)
            .build();

        // Save user first
        newUser = saveUser(newUser);

        // Save project
        project = saveProject(project);

        // Create user-project relationship with ADMIN role
        UserProject userProject = UserProject.builder()
                .id(new UserProjectId(newUser.getId(), project.getId()))
                .user(newUser)
                .project(project)
                .role("ADMIN")
                .joinedAt(ZonedDateTime.now())
                .build();

        userProjectRepository.save(userProject);

        // Set current project
        newUser.setCurrentProjectId(project.getId());
        return saveUser(newUser);
    }

    @Transactional
    public User createClientUser(ClientRegisterRequest request, String passwordHash) {
        // Create user
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordHash)
                .role(UserRole.USER)
                .job(request.getJob())
                .createdAt(ZonedDateTime.now())
                .build();

        user = userRepository.save(user);

        // Create project
        Project project = Project.builder()
                .name(request.getProjectName())
                .code(request.getProjectCode())
                .createdBy(user)
                .createdAt(ZonedDateTime.now())
                .build();

        project = projectRepository.save(project);

        // Create user-project relationship with ADMIN role
        UserProject userProject = UserProject.builder()
                .user(user)
                .project(project)
                .role("ADMIN")
                .joinedAt(ZonedDateTime.now())
                .build();

        userProjectRepository.save(userProject);

        // Set current project
        user.setCurrentProjectId(project.getId());
        return userRepository.save(user);
    }

    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<UserProject> getProjectUsers(String projectCode) {
        return projectRepository.findByCode(projectCode)
            .map(project -> userProjectRepository.findAllByProjectId(project.getId()))
            .orElse(List.of());
    }

    public void deleteUser(UUID uid) {
        userRepository.deleteById(uid);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public boolean hasProjectAccess(UUID userId, UUID projectId) {
        // First check if user is the project creator
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project != null && project.getCreatedBy() != null && project.getCreatedBy().getId().equals(userId)) {
            return true;
        }
        
        // Then check user_project table for membership
        return userProjectRepository.existsByUserIdAndProjectIdAndRole(userId, projectId, "ADMIN") ||
               userProjectRepository.existsByUserIdAndProjectIdAndRole(userId, projectId, "MEMBER");
    }

    public List<Project> getUserProjects(UUID userId) {
        return userProjectRepository.findAllByUserId(userId)
            .stream()
            .map(UserProject::getProject)
            .collect(Collectors.toList());
    }

    public String generateUniqueProjectCode() {
        String code;
        Random random = new Random();
        do {
            // Generate a random 3-digit number
            int randomNumber = random.nextInt(900) + 100; // This ensures a 3-digit number (100-999)
            code = String.format("PROJ-%03d", randomNumber);
        } while (projectRepository.existsByCode(code));
        return code;
    }

    public UserProject saveUserProject(UserProject userProject) {
        return userProjectRepository.save(userProject);
    }
} 