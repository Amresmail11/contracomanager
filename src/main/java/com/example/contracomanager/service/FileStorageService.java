package com.example.contracomanager.service;

import org.springframework.web.multipart.MultipartFile;
import com.example.contracomanager.model.DrawingFile;
import java.io.IOException;
import java.util.List;

public interface FileStorageService {
    DrawingFile storeFile(MultipartFile file, String projectCode) throws IOException;
    byte[] getFile(String fileName) throws IOException;
    void deleteFile(String fileName) throws IOException;
    List<DrawingFile> listFiles(String projectCode) throws IOException;
    String getFileUrl(String fileName);
} 