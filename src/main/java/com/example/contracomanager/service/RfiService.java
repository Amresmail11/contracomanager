package com.example.contracomanager.service;

import com.example.contracomanager.dto.rfi.CreateRfiRequest;
import com.example.contracomanager.dto.rfi.RfiResponse;
import com.example.contracomanager.dto.rfi.RfiReplyResponse;
import com.example.contracomanager.exception.ResourceNotFoundException;
import com.example.contracomanager.model.*;
import com.example.contracomanager.repository.GroupRepository;
import com.example.contracomanager.repository.ProjectRepository;
import com.example.contracomanager.repository.RfiRepository;
import com.example.contracomanager.repository.UserRepository;
import com.example.contracomanager.repository.UserProjectRepository;
import com.example.contracomanager.repository.GroupProjectUserRepository;
import com.example.contracomanager.repository.RfiGroupAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RfiService {
    private final RfiRepository rfiRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ProjectService projectService;
    private final UserProjectRepository userProjectRepository;
    private final GroupProjectUserRepository groupProjectUserRepository;
    private final RfiGroupAssignmentRepository rfiGroupAssignmentRepository;

    @Transactional
    public RfiResponse createRfi(CreateRfiRequest request, User creator) {
        // Validate project access
        if (!projectService.hasAccess(request.getProjectCode(), creator.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "You don't have access to project: " + request.getProjectCode());
        }

        // Find project
        Project project = projectRepository.findByCode(request.getProjectCode())
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with code: " + request.getProjectCode()));

        // Initialize variables
        User assignedUser = null;
        final Group assignedGroup = request.getAssignedGroupName() != null ?
            groupRepository.findByNameAndProjectCode(
                request.getAssignedGroupName(), request.getProjectCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Group not found with name: " + request.getAssignedGroupName()))
            : null;
        String assignedType = null;

        // Handle user assignment
        if (request.getAssignedToEmail() != null) {
            assignedUser = userRepository.findByEmail(request.getAssignedToEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getAssignedToEmail()));
            
            // Verify user is in project
            if (!projectService.hasAccess(request.getProjectCode(), assignedUser.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Assigned user is not a member of the project");
            }
            assignedType = "USER";
        }

        // Set group assignment type if group is assigned
        if (assignedGroup != null) {
            assignedType = "GROUP";
        }

        // Create RFI
        Rfi rfi = Rfi.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .status("PENDING")
            .priority(request.getPriority().toString())
            .project(project)
            .dueDate(request.getDueDate())
            .createdBy(creator)
            .assignedType(assignedType)
            .assignedToUser(assignedUser)
            .assignedToGroup(assignedGroup)
            .build();

        // Save RFI
        Rfi savedRfi = rfiRepository.save(rfi);
        log.info("Created RFI with ID: {}", savedRfi.getId());

        // Create individual assignments if assigned to group
        if (assignedGroup != null) {
            List<GroupProjectUser> groupMembers = groupProjectUserRepository.findAllByGroupId(assignedGroup.getId());
            List<RfiGroupAssignment> assignments = groupMembers.stream()
                .map(member -> RfiGroupAssignment.builder()
                    .rfi(savedRfi)
                    .user(member.getUser())
                    .group(assignedGroup)
                    .build())
                .toList();
            rfiGroupAssignmentRepository.saveAll(assignments);
            log.info("Created {} individual assignments for group members", assignments.size());
        }

        return toRfiResponse(savedRfi);
    }

    public RfiResponse updateRfi(UUID rfiId, Rfi rfiDetails) {
        Rfi rfi = rfiRepository.findById(rfiId)
            .orElseThrow(() -> new IllegalArgumentException("RFI not found"));

        rfi.setTitle(rfiDetails.getTitle());
        rfi.setDescription(rfiDetails.getDescription());
        rfi.setStatus(rfiDetails.getStatus());
        rfi.setPriority(rfiDetails.getPriority());
        rfi.setDueDate(rfiDetails.getDueDate());
        rfi.setAssignedType(rfiDetails.getAssignedType());
        rfi.setAssignedToUser(rfiDetails.getAssignedToUser());
        rfi.setAssignedToGroup(rfiDetails.getAssignedToGroup());

        Rfi savedRfi = rfiRepository.save(rfi);
        return toRfiResponse(savedRfi);
    }

    public Map<String, String> deleteRfi(UUID rfiId, User user) {
        Rfi rfi = rfiRepository.findById(rfiId)
            .orElseThrow(() -> new ResourceNotFoundException("RFI not found"));

        // Only creator can delete
        if (!rfi.getCreatedBy().getId().equals(user.getId())) {
            throw new AccessDeniedException("Only the creator can delete this RFI");
        }

        rfiRepository.delete(rfi);
        return Map.of("message", "RFI deleted successfully");
    }

    public Rfi getRfi(UUID rfiId) {
        return rfiRepository.findById(rfiId)
            .orElseThrow(() -> new ResourceNotFoundException("RFI not found"));
    }

    public Page<Rfi> getProjectRfis(UUID projectId, Pageable pageable) {
        return rfiRepository.findByProjectId(projectId, pageable);
    }

    public Page<Rfi> getProjectRfisByStatus(UUID projectId, String status, Pageable pageable) {
        return rfiRepository.findByProjectIdAndStatus(projectId, status, pageable);
    }

    public Page<Rfi> getUserAssignedRfis(User user, Pageable pageable) {
        Page<Rfi> directAssignments = rfiRepository.findByAssignedToUserAndAssignedType(user, "USER", pageable);
        List<RfiGroupAssignment> groupAssignments = rfiGroupAssignmentRepository.findAllByUserId(user.getId());
        List<Rfi> groupRfis = groupAssignments.stream()
            .map(RfiGroupAssignment::getRfi)
            .toList();

        List<Rfi> allAssignedRfis = new ArrayList<>();
        allAssignedRfis.addAll(directAssignments.getContent());
        allAssignedRfis.addAll(groupRfis);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allAssignedRfis.size());

        return new PageImpl<>(
            allAssignedRfis.subList(start, end),
            pageable,
            allAssignedRfis.size()
        );
    }

    public Page<Rfi> getUserCreatedRfis(User user, Pageable pageable) {
        return rfiRepository.findByCreatedBy(user, pageable);
    }

    public List<Rfi> getOverdueRfis() {
        return rfiRepository.findByDueDateBefore(LocalDateTime.now());
    }

    public List<Rfi> getProjectOverdueRfis(UUID projectId) {
        return rfiRepository.findByProjectIdAndDueDateBefore(projectId, LocalDateTime.now());
    }

    public Page<Rfi> getAllUserRelatedRfis(User user, Pageable pageable) {
        log.info("Getting all RFIs related to user ID: {} with username: {}", 
            user.getId(), user.getUsername());

        // Get RFIs created by user
        List<Rfi> createdRfis = rfiRepository.findByCreatedBy(user, pageable).getContent();
        log.debug("Found {} RFIs created by user", createdRfis.size());

        // Get RFIs directly assigned to user
        List<Rfi> directAssignments = rfiRepository.findByAssignedToUserAndAssignedType(user, "USER", pageable).getContent();
        log.debug("Found {} RFIs directly assigned to user", directAssignments.size());

        // Get RFIs assigned through groups
        List<RfiGroupAssignment> groupAssignments = rfiGroupAssignmentRepository.findAllByUserId(user.getId());
        List<Rfi> groupRfis = groupAssignments.stream()
            .map(RfiGroupAssignment::getRfi)
            .toList();
        log.debug("Found {} RFIs assigned through groups", groupRfis.size());

        // Get all projects user has access to
        List<UserProject> userProjects = userProjectRepository.findAllByUserId(user.getId());
        List<UUID> projectIds = userProjects.stream()
            .map(up -> up.getProject().getId())
            .toList();
        
        // Get RFIs from user's projects
        List<Rfi> projectRfis = projectIds.stream()
            .flatMap(projectId -> rfiRepository.findByProjectId(projectId, pageable).getContent().stream())
            .toList();
        log.debug("Found {} RFIs from user's projects", projectRfis.size());

        // Combine all RFIs and remove duplicates
        List<Rfi> allRfis = new ArrayList<>();
        allRfis.addAll(createdRfis);
        allRfis.addAll(directAssignments);
        allRfis.addAll(groupRfis);
        allRfis.addAll(projectRfis);
        
        // Remove duplicates by ID
        List<Rfi> uniqueRfis = allRfis.stream()
            .distinct()
            .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt())) // Sort by creation date, newest first
            .toList();
        log.info("Found {} unique RFIs in total", uniqueRfis.size());

        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), uniqueRfis.size());
        
        return new PageImpl<>(
            uniqueRfis.subList(start, end),
            pageable,
            uniqueRfis.size()
        );
    }

    public RfiResponse toRfiResponse(Rfi rfi) {
        log.debug("Converting RFI {} to response", rfi.getId());
        try {
            // Validate required fields
            if (rfi == null) {
                throw new IllegalArgumentException("RFI cannot be null");
            }
            if (rfi.getProject() == null) {
                throw new IllegalArgumentException("RFI project cannot be null");
            }
            if (rfi.getCreatedBy() == null) {
                throw new IllegalArgumentException("RFI creator cannot be null");
            }

            // Create creator info
            Map<String, Object> createdBy = new HashMap<>();
            createdBy.put("id", rfi.getCreatedBy().getId().toString());
            createdBy.put("email", rfi.getCreatedBy().getEmail());
            createdBy.put("job", rfi.getCreatedBy().getJob());

            // Create resolvedBy info if available
            Map<String, Object> resolvedBy = null;
            if (rfi.getResolvedBy() != null) {
                resolvedBy = new HashMap<>();
                resolvedBy.put("id", rfi.getResolvedBy().getId().toString());
                resolvedBy.put("email", rfi.getResolvedBy().getEmail());
                resolvedBy.put("job", rfi.getResolvedBy().getJob());
            }

            // Build response
            RfiResponse.RfiResponseBuilder builder = RfiResponse.builder()
                .id(rfi.getId())
                .title(rfi.getTitle())
                .description(rfi.getDescription())
                .status(rfi.getStatus())
                .priority(rfi.getPriority())
                .projectCode(rfi.getProject().getCode())
                .dueDate(rfi.getDueDate())
                .createdAt(rfi.getCreatedAt())
                .createdBy(createdBy)
                .resolvedBy(resolvedBy)
                .assignedType(rfi.getAssignedType());

            // Add assigned to info
            if ("USER".equals(rfi.getAssignedType()) && rfi.getAssignedToUser() != null) {
                builder.assignedTo(rfi.getAssignedToUser().getEmail());
            } else if ("GROUP".equals(rfi.getAssignedType()) && rfi.getAssignedToGroup() != null) {
                builder.assignedTo(rfi.getAssignedToGroup().getName());
            }

            // Add replies if present
            if (rfi.getReplies() != null && !rfi.getReplies().isEmpty()) {
                try {
                    List<RfiReplyResponse> replyResponses = rfi.getReplies().stream()
                        .filter(reply -> reply != null)
                        .map(this::toRfiReplyResponse)
                        .collect(Collectors.toList());
                    builder.replies(replyResponses);
                } catch (Exception e) {
                    log.error("Error processing replies for RFI {}: {}", rfi.getId(), e.getMessage());
                    // Continue without replies rather than failing the entire response
                }
            }

            return builder.build();
        } catch (Exception e) {
            log.error("Error converting RFI {} to response: {}", rfi.getId(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error processing RFI: " + e.getMessage());
        }
    }

    @Transactional
    public RfiResponse resolveRfi(UUID rfiId, User user, String message) {
        Rfi rfi = rfiRepository.findById(rfiId)
                .orElseThrow(() -> new ResourceNotFoundException("RFI not found"));

        // Set status to RESOLVED and add who resolved it
        rfi.setStatus("RESOLVED");
        rfi.setResolvedBy(user);

        // Create resolution reply
        RfiReply reply = RfiReply.builder()
                .message(message)
                .createdBy(user)
                .rfi(rfi)
                .build();
        rfi.addReply(reply);

        // Save the RFI
        Rfi savedRfi = rfiRepository.save(rfi);
        return toRfiResponse(savedRfi);
    }

    public RfiReplyResponse toRfiReplyResponse(RfiReply reply) {
        log.debug("Converting RFI reply {} to response", reply.getId());
        try {
            // Create creator info
            Map<String, Object> createdBy = new HashMap<>();
            createdBy.put("id", reply.getCreatedBy().getId().toString());
            createdBy.put("email", reply.getCreatedBy().getEmail());
            createdBy.put("job", reply.getCreatedBy().getJob());

            return RfiReplyResponse.builder()
                .id(reply.getId())
                .content(reply.getMessage())
                .createdAt(reply.getCreatedAt())
                .createdBy(createdBy)
                .build();
        } catch (Exception e) {
            log.error("Error converting RFI reply {} to response: ", reply.getId(), e);
            throw e;
        }
    }
} 