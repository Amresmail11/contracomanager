package com.example.contracomanager.controller;

import com.example.contracomanager.model.Project;
import com.example.contracomanager.model.User;
import com.example.contracomanager.model.UserProject;
import com.example.contracomanager.model.UserProjectId;
import com.example.contracomanager.service.DatabaseService;
import com.example.contracomanager.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import java.time.ZonedDateTime;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {
    private final DatabaseService databaseService;

    @GetMapping("/my-projects")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getUserProjects(@AuthenticationPrincipal SecurityUser securityUser) {
        try {
            // Get fresh user instance to avoid lazy loading issues
            User user = databaseService.getUserByUid(securityUser.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Get all projects for the user from UserProject table
            List<Project> userProjects = databaseService.getUserProjects(user.getId());

            // Map projects to response format with additional fields
            List<Map<String, Object>> projectsList = userProjects.stream()
                .map(project -> {
                    Map<String, Object> projectMap = new HashMap<>();
                    projectMap.put("name", project.getName());
                    projectMap.put("code", project.getCode());
                    projectMap.put("dueDate", project.getDueDate() != null ? project.getDueDate().toString() : "");
                    projectMap.put("projectOwner", project.getProjectOwner() != null ? project.getProjectOwner() : "");
                    projectMap.put("address", project.getAddress() != null ? project.getAddress() : "");
                    return projectMap;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "projects", projectsList
            ));
        } catch (Exception e) {
            log.error("Error fetching user projects: ", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
                ));
        }
    }

    @PostMapping("/create")
    @Transactional
    public ResponseEntity<?> createProject(@RequestBody Map<String, String> request, @AuthenticationPrincipal SecurityUser securityUser) {
        try {
            log.info("Creating project with request: {}", request);
            
            // Validate required fields
            if (request.get("name") == null || request.get("name").trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Project name is required"));
            }

            // Parse due date if provided
            ZonedDateTime dueDate = null;
            String dueDateStr = request.get("dueDate");
            if (dueDateStr != null && !dueDateStr.trim().isEmpty()) {
                dueDate = ZonedDateTime.parse(dueDateStr);
            }

            // Create project
            Project project = Project.builder()
                .name(request.get("name"))
                .projectOwner(request.get("projectOwner"))
                .dueDate(dueDate)
                .address(request.get("address"))
                .createdBy(securityUser.getUser())
                .build();

            // Save project first
            project = databaseService.saveProject(project);
            log.info("Saved project: {}", project);

            // Create user-project relationship with ADMIN role
            UserProjectId userProjectId = new UserProjectId(securityUser.getUser().getId(), project.getId());
            UserProject userProject = UserProject.builder()
                .id(userProjectId)
                .user(securityUser.getUser())
                .project(project)
                .role("ADMIN")
                .joinedAt(ZonedDateTime.now())
                .build();

            databaseService.saveUserProject(userProject);

            // Create response without accessing lazy collections
            Map<String, Object> projectMap = new HashMap<>();
            projectMap.put("id", project.getId());
            projectMap.put("name", project.getName());
            projectMap.put("code", project.getCode());
            projectMap.put("createdBy", Map.of(
                "id", securityUser.getUser().getId(),
                "username", securityUser.getUser().getUsername()
            ));
            projectMap.put("dueDate", project.getDueDate() != null ? project.getDueDate().toString() : "");
            projectMap.put("projectOwner", project.getProjectOwner() != null ? project.getProjectOwner() : "");
            projectMap.put("address", project.getAddress() != null ? project.getAddress() : "");

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "project", projectMap
            ));
        } catch (Exception e) {
            log.error("Error creating project: ", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "status", "error", 
                    "message", e.getMessage()
                ));
        }
    }

    @PostMapping("/join/{projectCode}")
    @Transactional
    public ResponseEntity<?> joinProject(
            @PathVariable String projectCode,
            @AuthenticationPrincipal SecurityUser securityUser) {
        try {
            // Validate project code format
            if (!projectCode.matches("PROJ-\\d{3}")) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "status", "error",
                        "message", "Invalid project code format. Must be in format PROJ-XXX where X is a digit"
                    ));
            }

            User user = databaseService.getUserByUid(securityUser.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Project project = databaseService.getProjectByCode(projectCode)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with code: " + projectCode));

            // Check if user is already a member
            if (project.getMembers().stream().anyMatch(member -> member.getId().equals(user.getId()))) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "status", "error",
                        "message", "You are already a member of this project"
                    ));
            }

            // Create user-project relationship with MEMBER role
            UserProjectId userProjectId = new UserProjectId(user.getId(), project.getId());
            UserProject userProject = UserProject.builder()
                .id(userProjectId)
                .user(user)
                .project(project)
                .role("MEMBER")
                .joinedAt(ZonedDateTime.now())
                .build();

            databaseService.saveUserProject(userProject);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Successfully joined the project",
                "project", Map.of(
                    "name", project.getName(),
                    "code", project.getCode(),
                    "owner", project.getProjectOwner()
                )
            ));
        } catch (Exception e) {
            log.error("Error joining project: ", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
                ));
        }
    }
} 