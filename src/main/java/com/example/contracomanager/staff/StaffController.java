package com.example.contracomanager.staff;

import com.example.contracomanager.model.Project;
import com.example.contracomanager.model.User;
import com.example.contracomanager.model.UserRole;
import com.example.contracomanager.service.DatabaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@Slf4j
public class StaffController {
    private final DatabaseService databaseService;

    @GetMapping("/projects")
    public ResponseEntity<?> getProjects(@RequestHeader("X-User-ID") UUID userId) {
        try {
            User user = databaseService.getUserByUid(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (user.getRole() != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("status", "error", "message", "Access denied"));
            }

            List<Project> projects = databaseService.getAllProjects();
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "projects", projects
            ));
        } catch (Exception e) {
            log.error("Error fetching projects: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@RequestHeader("X-User-ID") UUID userId) {
        try {
            User user = databaseService.getUserByUid(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (user.getRole() != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("status", "error", "message", "Access denied"));
            }

            List<User> users = databaseService.getAllUsers();
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "users", users
            ));
        } catch (Exception e) {
            log.error("Error fetching users: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
} 