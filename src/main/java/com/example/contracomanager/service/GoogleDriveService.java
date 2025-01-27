package com.example.contracomanager.service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleDriveService {
    private final Drive driveService;

    public String createFolder(String folderName) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        File file = driveService.files().create(fileMetadata)
            .setFields("id")
            .execute();
        log.info("Created folder: {} with ID: {}", folderName, file.getId());
        return file.getId();
    }

    public String uploadFile(MultipartFile file, String folderId) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(file.getOriginalFilename());
        fileMetadata.setParents(Collections.singletonList(folderId));

        InputStreamContent mediaContent = new InputStreamContent(
            file.getContentType(),
            new ByteArrayInputStream(file.getBytes())
        );

        File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
            .setFields("id, webViewLink")
            .execute();
        log.info("Uploaded file: {} to folder: {}", file.getOriginalFilename(), folderId);
        return uploadedFile.getId();
    }

    public List<File> listFiles(String folderId) throws IOException {
        String query = String.format("'%s' in parents", folderId);
        FileList result = driveService.files().list()
            .setQ(query)
            .setFields("files(id, name, webViewLink, createdTime, size)")
            .execute();
        return result.getFiles();
    }

    public void deleteFile(String fileId) throws IOException {
        try {
            driveService.files().delete(fileId).execute();
            log.info("Deleted file: {}", fileId);
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 404) {
                log.warn("File not found: {}", fileId);
            } else {
                throw e;
            }
        }
    }

    public byte[] downloadFile(String fileId) throws IOException {
        return driveService.files().get(fileId)
            .executeMediaAsInputStream()
            .readAllBytes();
    }
} 