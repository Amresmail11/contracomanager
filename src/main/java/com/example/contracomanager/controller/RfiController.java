package com.example.contracomanager.controller;

import com.example.contracomanager.dto.rfi.*;
import com.example.contracomanager.exception.ResourceNotFoundException;
import com.example.contracomanager.model.*;
import com.example.contracomanager.security.SecurityUser;
import com.example.contracomanager.service.DatabaseService;
import com.example.contracomanager.service.RfiService;
import com.example.contracomanager.service.TranslatorService;
import com.example.contracomanager.repository.UserRepository;
import com.example.contracomanager.repository.ProjectRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RfiController {
    private final RfiService rfiService;
    private final DatabaseService databaseService;
    private final TranslatorService translatorService;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @PostMapping("/api/rfis")
    public ResponseEntity<?> createRfi(
            @Valid @RequestBody CreateRfiByNameRequest request,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        try {
            log.info("Creating RFI for project: {}, title: {}", 
                request.getProjectCode(), request.getTitle());
            
            return handleRfiCreation(request, securityUser);
        } catch (Exception e) {
            log.error("Error creating RFI: ", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
                ));
        }
    }

    private ResponseEntity<?> handleRfiCreation(
            CreateRfiByNameRequest request,
            SecurityUser securityUser
    ) {
        // Validate that either email or group name is provided
        if (request.getAssignedToEmail() == null && request.getAssignedGroupName() == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error", "message", "Either user email or group must be assigned"));
        }

        // Validate that not both are provided
        if (request.getAssignedToEmail() != null && request.getAssignedGroupName() != null) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error", "message", "Cannot assign to both user and group"));
        }

        try {
            // Convert request to service request
            CreateRfiRequest serviceRequest = new CreateRfiRequest();
            serviceRequest.setTitle(request.getTitle());
            serviceRequest.setDescription(request.getDescription());
            serviceRequest.setPriority(request.getPriority());
            serviceRequest.setProjectCode(request.getProjectCode());
            serviceRequest.setDueDate(request.getDeadline() != null ? 
                request.getDeadline().atStartOfDay() : null);
            serviceRequest.setAssignedToEmail(request.getAssignedToEmail());
            serviceRequest.setAssignedGroupName(request.getAssignedGroupName());

            RfiResponse rfiResponse = rfiService.createRfi(serviceRequest, securityUser.getUser());
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "RFI created successfully",
                "data", rfiResponse
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                .body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
                ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
                ));
        }
    }

    @PutMapping("/api/rfis/{rfiId}")
    public ResponseEntity<?> updateRfi(
            @PathVariable UUID rfiId,
            @Valid @RequestBody UpdateRfiRequest request,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        try {
            log.info("Updating RFI {} with request: {}", rfiId, request);

            // Get RFI
            Rfi rfi = rfiService.getRfi(rfiId);

            // Verify access
            if (!databaseService.hasProjectAccess(securityUser.getUser().getId(), rfi.getProject().getId())) {
                return ResponseEntity.status(403)
                    .body(Map.of("status", "error", "message", "You don't have access to this RFI"));
            }

            // Update RFI
            if (request.getTitle() != null) {
                rfi.setTitle(request.getTitle());
            }
            if (request.getDescription() != null) {
                rfi.setDescription(request.getDescription());
            }
            if (request.getPriority() != null) {
                rfi.setPriority(request.getPriority().toString());
            }
            if (request.getStatus() != null) {
                rfi.setStatus(request.getStatus().toString());
            }
            if (request.getDueDate() != null) {
                rfi.setDueDate(request.getDueDate());
            }
            if (request.getAssignedToEmail() != null) {
                User assignedUser = userRepository.findByEmail(request.getAssignedToEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getAssignedToEmail()));
                
                // Verify user is in project
                if (!databaseService.hasProjectAccess(assignedUser.getId(), rfi.getProject().getId())) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("status", "error", "message", "Assigned user is not a member of the project"));
                }
                
                rfi.setAssignedToUser(assignedUser);
                rfi.setAssignedType("USER");
                rfi.setAssignedToGroup(null);
            }

            // Save and return updated RFI
            RfiResponse response = rfiService.updateRfi(rfiId, rfi);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "RFI updated successfully",
                "data", response
            ));
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(404)
                .body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating RFI: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("status", "error", "message", "Error updating RFI"));
        }
    }

    @DeleteMapping("/api/rfis/{rfiId}")
    public ResponseEntity<Map<String, Object>> deleteRfi(
            @PathVariable UUID rfiId,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        try {
            Map<String, String> response = rfiService.deleteRfi(rfiId, securityUser.getUser());
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", response.get("message")
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error deleting RFI: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/api/rfis/{rfiId}")
    public ResponseEntity<?> getRfi(
            @PathVariable UUID rfiId,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        try {
            Rfi rfi = rfiService.getRfi(rfiId);
            
            // Check if user has access to the project
            if (!databaseService.hasProjectAccess(securityUser.getUser().getId(), rfi.getProject().getId())) {
                return ResponseEntity.status(403)
                    .body(Map.of(
                        "status", "error",
                        "message", "You don't have access to this RFI"
                    ));
            }
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", rfiService.toRfiResponse(rfi)
            ));
        } catch (Exception e) {
            log.error("Error fetching RFI: ", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
                ));
        }
    }

    @GetMapping("/api/projects/{projectCode}/rfis")
    public ResponseEntity<?> getProjectRfis(
            @PathVariable String projectCode,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal SecurityUser securityUser,
            Pageable pageable
    ) {
        try {
            log.info("Fetching RFIs for project code: {}, status: {}, user: {}", 
                projectCode, status, securityUser.getUsername());
            
            // Get project by code
            Project project = projectRepository.findByCode(projectCode)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with code: " + projectCode));
            
            log.info("Found project with ID: {}", project.getId());
            
            if (!databaseService.hasProjectAccess(securityUser.getUser().getId(), project.getId())) {
                return ResponseEntity.status(403)
                    .body(Map.of(
                        "status", "error",
                        "message", "You don't have access to this project"
                    ));
            }

            Page<Rfi> rfis;
            try {
                if (status != null && !"undefined".equals(status)) {
                    log.info("Fetching RFIs with status filter: {}", status);
                    rfis = rfiService.getProjectRfisByStatus(project.getId(), status, pageable);
                } else {
                    log.info("Fetching all RFIs");
                    rfis = rfiService.getProjectRfis(project.getId(), pageable);
                }
                log.info("Successfully fetched {} RFIs", rfis.getTotalElements());
            } catch (Exception e) {
                log.error("Error fetching RFIs from service: ", e);
                throw e;
            }
            
            try {
                Page<RfiResponse> rfiResponses = rfis.map(rfi -> {
                    try {
                        return rfiService.toRfiResponse(rfi);
                    } catch (Exception e) {
                        log.error("Error converting RFI to response: {}", rfi.getId(), e);
                        throw e;
                    }
                });
                
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", rfiResponses
                ));
            } catch (Exception e) {
                log.error("Error mapping RFIs to responses: ", e);
                throw e;
            }
        } catch (Exception e) {
            log.error("Error fetching project RFIs: ", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
                ));
        }
    }

    @GetMapping("/api/rfis/assigned")
    public ResponseEntity<?> getAssignedRfis(
            @AuthenticationPrincipal SecurityUser securityUser,
            Pageable pageable
    ) {
        try {
            User user = securityUser.getUser();
            log.info("Getting assigned RFIs for user ID: {} with username: {}", 
                user.getId(), user.getUsername());
            Page<Rfi> rfis = rfiService.getUserAssignedRfis(user, pageable);
            log.info("Found {} assigned RFIs", rfis.getTotalElements());
            Page<RfiResponse> rfiResponses = rfis.map(rfiService::toRfiResponse);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", rfiResponses
            ));
        } catch (Exception e) {
            log.error("Error fetching assigned RFIs: ", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
                ));
        }
    }

    @GetMapping("/api/rfis/created")
    public ResponseEntity<?> getCreatedRfis(
            @AuthenticationPrincipal SecurityUser securityUser,
            Pageable pageable
    ) {
        try {
            User user = securityUser.getUser();
            log.info("Getting created RFIs for user ID: {} with username: {}", 
                user.getId(), user.getUsername());
            Page<Rfi> rfis = rfiService.getUserCreatedRfis(user, pageable);
            log.info("Found {} created RFIs", rfis.getTotalElements());
            Page<RfiResponse> rfiResponses = rfis.map(rfiService::toRfiResponse);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", rfiResponses
            ));
        } catch (Exception e) {
            log.error("Error fetching created RFIs: ", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
                ));
        }
    }

    @GetMapping("/api/rfis/overdue")
    public ResponseEntity<?> getOverdueRfis(
            @RequestParam(required = false) UUID projectId,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        try {
            List<Rfi> rfis;
            if (projectId != null) {
                // Check if user has access to the project
                Project project = databaseService.getProjectById(projectId)
                    .orElseThrow(() -> new IllegalArgumentException("Project not found"));
                
                if (!databaseService.hasProjectAccess(securityUser.getUser().getId(), project.getId())) {
                    return ResponseEntity.status(403)
                        .body(Map.of(
                            "status", "error",
                            "message", "You don't have access to this project"
                        ));
                }
                
                rfis = rfiService.getProjectOverdueRfis(projectId);
            } else {
                rfis = rfiService.getOverdueRfis();
            }
            
            List<RfiResponse> rfiResponses = rfis.stream()
                .map(rfiService::toRfiResponse)
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "rfis", rfiResponses
            ));
        } catch (Exception e) {
            log.error("Error fetching overdue RFIs: ", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
                ));
        }
    }

    @PutMapping("/api/rfis/{rfiId}/resolve")
    public ResponseEntity<Map<String, Object>> resolveRfi(
            @PathVariable UUID rfiId,
            @Valid @RequestBody ResolveRfiRequest request,
            @AuthenticationPrincipal SecurityUser securityUser) {
        try {
            log.info("Resolving RFI {} with message", rfiId);
            RfiResponse response = rfiService.resolveRfi(rfiId, securityUser.getUser(), request.getMessage());
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "RFI resolved successfully",
                    "data", response
            ));
        } catch (AccessDeniedException e) {
            log.warn("Access denied while resolving RFI {}: {}", rfiId, e.getMessage());
            return ResponseEntity.status(403)
                .body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
                ));
        } catch (ResourceNotFoundException e) {
            log.warn("Resource not found while resolving RFI {}: {}", rfiId, e.getMessage());
            return ResponseEntity.status(404)
                .body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            log.error("Error resolving RFI {}: ", rfiId, e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
                ));
        }
    }

    @GetMapping("/api/rfis/all")
    public ResponseEntity<?> getAllUserRfis(
            @AuthenticationPrincipal SecurityUser securityUser,
            Pageable pageable
    ) {
        try {
            User user = securityUser.getUser();
            log.info("Getting all RFIs for user ID: {} with username: {}", 
                user.getId(), user.getUsername());
            Page<Rfi> rfis = rfiService.getAllUserRelatedRfis(user, pageable);
            log.info("Found {} total RFIs", rfis.getTotalElements());
            Page<RfiResponse> rfiResponses = rfis.map(rfiService::toRfiResponse);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", rfiResponses
            ));
        } catch (Exception e) {
            log.error("Error fetching all RFIs: ", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
                ));
        }
    }
} 