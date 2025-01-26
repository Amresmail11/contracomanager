package com.example.contracomanager.controller;

import com.example.contracomanager.dto.group.CreateGroupRequest;
import com.example.contracomanager.dto.group.AddGroupMembersRequest;
import com.example.contracomanager.exception.ResourceNotFoundException;
import com.example.contracomanager.security.SecurityUser;
import com.example.contracomanager.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        try {
            Map<String, String> response = groupService.createGroup(request, securityUser.getUser());
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", response.get("message")
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                "status", "error",
                "message", e.getReason()
            ));
        }
    }

    @GetMapping("/my-groups")
    public ResponseEntity<Map<String, Object>> getUserGroups(
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        Map<String, Object> response = groupService.getUserGroups(securityUser.getUser());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Map<String, Object>> deleteGroup(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        try {
            Map<String, String> response = groupService.deleteGroup(groupId, securityUser.getUser());
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", response.get("message")
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                "status", "error",
                "message", e.getReason()
            ));
        }
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<Map<String, Object>> addGroupMembers(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddGroupMembersRequest request,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        try {
            Map<String, String> response = groupService.addGroupMembers(groupId, request, securityUser.getUser());
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", response.get("message")
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                "status", "error",
                "message", e.getReason()
            ));
        }
    }

    @GetMapping("/project/{projectCode}")
    public ResponseEntity<Map<String, Object>> getProjectGroups(
            @PathVariable String projectCode,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        try {
            Map<String, Object> response = groupService.getProjectGroups(projectCode, securityUser.getUser());
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                "status", "error",
                "message", e.getReason()
            ));
        }
    }
} 