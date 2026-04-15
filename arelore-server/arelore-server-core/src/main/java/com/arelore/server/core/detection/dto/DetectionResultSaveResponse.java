package com.arelore.server.core.detection.dto;

import lombok.Data;

@Data
public class DetectionResultSaveResponse {
    private String detectResult;
    private String extraInfo;
}

