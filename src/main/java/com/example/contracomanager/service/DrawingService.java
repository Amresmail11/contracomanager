package com.example.contracomanager.service;

import com.example.contracomanager.model.DrawingFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DrawingService {
    private final FileStorageService fileStorageService;

    public DrawingFile uploadDrawing(MultipartFile drawing, String projectCode, String uid) throws IOException {
        log.info("Uploading drawing {} for project {}", drawing.getOriginalFilename(), projectCode);
        return fileStorageService.storeFile(drawing, projectCode);
    }

    public List<DrawingFile> listDrawings(String projectCode, String uid) throws IOException {
        log.info("Listing drawings for project {}", projectCode);
        return fileStorageService.listFiles(projectCode);
    }

    public byte[] downloadDrawing(String fileId, String uid, String projectCode) throws IOException {
        log.info("Downloading drawing {} from project {}", fileId, projectCode);
        return fileStorageService.getFile(fileId);
    }

    public void deleteDrawing(String fileId, String projectCode) throws IOException {
        log.info("Deleting drawing {} from project {}", fileId, projectCode);
        fileStorageService.deleteFile(fileId);
    }
}