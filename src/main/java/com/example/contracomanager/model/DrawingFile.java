package com.example.contracomanager.model;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DrawingFile {
    private String id;
    private String name;
    private String webViewLink;
    private ZonedDateTime createdTime;
    private Long size;
} 