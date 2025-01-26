package com.example.contracomanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrawingFile {
    private String id;
    private String name;
    private String webViewLink;
    private String previewLink;
    private String thumbnailLink;
    private ZonedDateTime createdTime;
    private Long size;
} 