package com.example.contracomanager.service;

import com.example.contracomanager.model.DrawingFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LocalFileStorageService implements FileStorageService {
    private final Path fileStorageLocation;
    private final String baseUrl;

    public LocalFileStorageService(
            @Value("${file.upload-dir}") String uploadDir,
            @Value("${app.base-url}") String baseUrl) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;
        
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    public DrawingFile storeFile(MultipartFile file, String projectCode) throws IOException {
        Path projectDir = fileStorageLocation.resolve(projectCode);
        Files.createDirectories(projectDir);

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + "_" + originalFileName;
        
        Path targetLocation = projectDir.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return DrawingFile.builder()
                .id(fileName)
                .name(originalFileName)
                .webViewLink(getFileUrl(fileName))
                .createdTime(ZonedDateTime.now())
                .size(file.getSize())
                .build();
    }

    @Override
    public byte[] getFile(String fileName) throws IOException {
        Path filePath = findFile(fileName);
        return Files.readAllBytes(filePath);
    }

    @Override
    public void deleteFile(String fileName) throws IOException {
        Path filePath = findFile(fileName);
        Files.delete(filePath);
    }

    @Override
    public List<DrawingFile> listFiles(String projectCode) throws IOException {
        Path projectDir = fileStorageLocation.resolve(projectCode);
        
        if (!Files.exists(projectDir)) {
            return new ArrayList<>();
        }

        return Files.list(projectDir)
                .filter(Files::isRegularFile)
                .map(path -> {
                    try {
                        String fileName = path.getFileName().toString();
                        String originalName = fileName.substring(fileName.indexOf('_') + 1);
                        return DrawingFile.builder()
                                .id(fileName)
                                .name(originalName)
                                .webViewLink(getFileUrl(fileName))
                                .createdTime(ZonedDateTime.now())
                                .size(Files.size(path))
                                .build();
                    } catch (IOException e) {
                        throw new RuntimeException("Error reading file: " + path, e);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public String getFileUrl(String fileName) {
        return ServletUriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/drawings/view/")
                .path(fileName)
                .toUriString();
    }

    private Path findFile(String fileName) throws IOException {
        return Files.find(fileStorageLocation, 
                Integer.MAX_VALUE,
                (path, attr) -> path.getFileName().toString().equals(fileName))
                .findFirst()
                .orElseThrow(() -> new IOException("File not found: " + fileName));
    }
} 