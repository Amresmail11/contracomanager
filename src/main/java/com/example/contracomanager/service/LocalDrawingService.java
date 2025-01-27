package com.example.contracomanager.service;

import com.example.contracomanager.dto.DrawingFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class LocalDrawingService {
    private final Path drawingsRoot;
    private static final String PROJECT_CODE_PATTERN = "^\\d{6}$";

    public LocalDrawingService(@Value("${app.drawings.upload-dir:drawings}") String uploadDir) {
        this.drawingsRoot = Paths.get(uploadDir);
        initializeDirectories();
    }

    private void initializeDirectories() {
        try {
            Files.createDirectories(drawingsRoot);
            log.info("Drawings directory initialized at: {}", drawingsRoot.toAbsolutePath());
        } catch (IOException e) {
            log.error("Could not initialize drawings directory", e);
            throw new RuntimeException("Could not initialize drawings directory", e);
        }
    }

    private void validateProjectCode(String projectCode) {
        if (projectCode == null || !projectCode.matches(PROJECT_CODE_PATTERN)) {
            throw new IllegalArgumentException("Project code must be exactly 6 numbers");
        }
    }

    private Path getProjectPath(String projectCode) {
        validateProjectCode(projectCode);
        Path projectPath = drawingsRoot.resolve(projectCode);
        try {
            Files.createDirectories(projectPath);
        } catch (IOException e) {
            log.error("Could not create project directory: {}", projectCode, e);
            throw new RuntimeException("Could not create project directory", e);
        }
        return projectPath;
    }

    public String uploadDrawing(MultipartFile drawing, String projectCode, String uid) throws IOException {
        validateProjectCode(projectCode);
        Path projectPath = getProjectPath(projectCode);
        
        // Generate unique filename
        String originalFilename = drawing.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String filename = UUID.randomUUID().toString() + extension;
        
        Path filePath = projectPath.resolve(filename);
        
        // Save the file
        Files.copy(drawing.getInputStream(), filePath);
        
        log.info("Uploaded file: {} to project: {}", filename, projectCode);
        return filePath.toAbsolutePath().toString();
    }

    public List<DrawingFile> listDrawings(String projectCode, String uid) throws IOException {
        validateProjectCode(projectCode);
        Path projectPath = getProjectPath(projectCode);
        List<DrawingFile> drawings = new ArrayList<>();

        if (Files.exists(projectPath)) {
            Files.list(projectPath).forEach(path -> {
                File file = path.toFile();
                DrawingFile drawing = DrawingFile.builder()
                    .id(file.getName())
                    .name(file.getName())
                    .webViewLink(file.getAbsolutePath())
                    .createdTime(ZonedDateTime.ofInstant(
                        Instant.ofEpochMilli(file.lastModified()),
                        ZoneId.systemDefault()))
                    .size(file.length())
                    .build();
                drawings.add(drawing);
            });
        }

        log.info("Listed {} drawings for project: {}", drawings.size(), projectCode);
        return drawings;
    }

    public byte[] downloadDrawing(String fileId, String uid, String projectCode) throws IOException {
        validateProjectCode(projectCode);
        Path projectPath = getProjectPath(projectCode);
        Path filePath = projectPath.resolve(fileId);
        
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + fileId);
        }
        
        if (!filePath.startsWith(projectPath)) {
            throw new SecurityException("File does not belong to the specified project");
        }
        
        log.info("Downloaded file: {} from project: {}", fileId, projectCode);
        return Files.readAllBytes(filePath);
    }
} 