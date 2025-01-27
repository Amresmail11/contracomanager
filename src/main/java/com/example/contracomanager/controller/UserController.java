package com.example.contracomanager.controller;

import com.example.contracomanager.model.Project;
import com.example.contracomanager.model.User;
import com.example.contracomanager.model.UserProject;
import com.example.contracomanager.service.DatabaseService;
import com.example.contracomanager.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Collections;

@RestController
@RequestMapping("/api/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final DatabaseService databaseService;

    @GetMapping("/same-project/{projectCode}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getUsersInSameProject(
            @PathVariable String projectCode) {
        try {
            // Get project by code first
            Project project = databaseService.getProjectByCode(projectCode)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with code: " + projectCode));

            // Check if user has access to the project
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.debug("No authentication found in security context");
                return ResponseEntity.status(401)
                    .body(Map.of(
                        "status", "error",
                        "message", "User is not authenticated"
                    ));
            }
            
            Object principal = authentication.getPrincipal();
            log.debug("Principal class: {}", principal.getClass().getName());
            
            if (!(principal instanceof SecurityUser)) {
                log.error("Principal is not an instance of SecurityUser");
                return ResponseEntity.status(500)
                    .body(Map.of(
                        "status", "error",
                        "message", "Invalid authentication type"
                    ));
            }

            SecurityUser securityUser = (SecurityUser) principal;
            if (!databaseService.hasProjectAccess(securityUser.getUser().getId(), project.getId())) {
                log.debug("User {} does not have access to project {}", securityUser.getUser().getId(), projectCode);
                return ResponseEntity.status(403)
                    .body(Map.of(
                        "status", "error",
                        "message", "Access denied to this project"
                    ));
            }
            
            // Get all users in the project
            List<UserProject> projectUsers = databaseService.getProjectUsers(projectCode);
            
            // Map users to DTOs with null checks
            List<Map<String, Object>> userDtos = projectUsers.stream()
                .filter(userProject -> userProject != null && userProject.getUser() != null)
                .map(userProject -> {
                    User user = userProject.getUser();
                    Map<String, Object> userDto = new HashMap<>();
                    userDto.put("id", user.getId());
                    userDto.put("username", user.getUsername());
                    userDto.put("email", user.getEmail());
                    userDto.put("role", userProject.getRole());
                    userDto.put("job", user.getJob() != null ? user.getJob() : "");
                    return userDto;
                })
                .collect(Collectors.toList());
            
            if (userDtos.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "users", Collections.emptyList()
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "users", userDtos
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("Project not found with code {}: {}", projectCode, e.getMessage());
            return ResponseEntity.status(404)
                .body(Map.of(
                    "status", "error",
                    "message", "Project not found with code: " + projectCode
                ));
        } catch (Exception e) {
            log.error("Error fetching project users for project {}: {}", projectCode, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "status", "error",
                    "message", "An error occurred while fetching project users"
                ));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        log.debug("Getting user profile");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401)
                .body(Map.of("status", "error", "message", "User not authenticated"));
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof SecurityUser)) {
            return ResponseEntity.status(500)
                .body(Map.of("status", "error", "message", "Invalid user details"));
        }

        User user = ((SecurityUser) principal).getUser();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("status", "success");
        userMap.put("id", user.getId());
        userMap.put("email", user.getEmail());
        userMap.put("username", user.getUserUsername());
        userMap.put("role", user.getRole());
        userMap.put("job", user.getJob());
        userMap.put("currentProjectId", user.getCurrentProjectId());

        return ResponseEntity.ok(userMap);
    }
} 