package com.example.contracomanager.controller;

import com.example.contracomanager.dto.DrawingFile;
import com.example.contracomanager.model.Project;
import com.example.contracomanager.service.DatabaseService;
import com.example.contracomanager.service.LocalDrawingService;
import com.example.contracomanager.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/drawings")
@RequiredArgsConstructor
@Slf4j
public class DrawingController {
    private final LocalDrawingService drawingService;
    private final DatabaseService databaseService;

    @PostMapping("/upload/{projectCode}")
    public ResponseEntity<?> uploadDrawing(
            @PathVariable String projectCode,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal SecurityUser securityUser) throws IOException {
        try {
            Project project = databaseService.getProjectByCode(projectCode)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

            if (!databaseService.hasProjectAccess(securityUser.getUser().getId(), project.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("status", "error", "message", "No access to this project"));
            }

            String filePath = drawingService.uploadDrawing(file, projectCode, securityUser.getUser().getId().toString());
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "filePath", filePath
            ));
        } catch (IllegalArgumentException e) {
            log.error("Project not found: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error uploading drawing: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Failed to upload drawing: " + e.getMessage()));
        }
    }

    @GetMapping("/download/{projectCode}/{fileId}")
    public ResponseEntity<?> downloadDrawing(
            @PathVariable String projectCode,
            @PathVariable String fileId,
            @AuthenticationPrincipal SecurityUser securityUser) throws IOException {
        try {
            Project project = databaseService.getProjectByCode(projectCode)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

            if (!databaseService.hasProjectAccess(securityUser.getUser().getId(), project.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("status", "error", "message", "No access to this project"));
            }

            byte[] fileContent = drawingService.downloadDrawing(fileId, securityUser.getUser().getId().toString(), projectCode);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileContent);
        } catch (IllegalArgumentException e) {
            log.error("Project not found: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error downloading drawing: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Failed to download drawing: " + e.getMessage()));
        }
    }

    @GetMapping("/list/{projectCode}")
    public ResponseEntity<?> listDrawings(
            @PathVariable String projectCode,
            @AuthenticationPrincipal SecurityUser securityUser) throws IOException {
        try {
            Project project = databaseService.getProjectByCode(projectCode)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

            if (!databaseService.hasProjectAccess(securityUser.getUser().getId(), project.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("status", "error", "message", "No access to this project"));
            }

            List<DrawingFile> drawings = drawingService.listDrawings(projectCode, securityUser.getUser().getId().toString());
            
            List<Map<String, Object>> drawingsList = drawings.stream()
                .map(drawing -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", drawing.getId());
                    map.put("name", drawing.getName());
                    map.put("filePath", drawing.getWebViewLink());
                    if (drawing.getCreatedTime() != null) {
                        map.put("createdTime", drawing.getCreatedTime());
                    }
                    if (drawing.getSize() != null) {
                        map.put("size", drawing.getSize());
                    }
                    return map;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "drawings", drawingsList
            ));
        } catch (IllegalArgumentException e) {
            log.error("Project not found: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error listing drawings: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Failed to list drawings: " + e.getMessage()));
        }
    }
} 