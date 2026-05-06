package com.arelore.server.core.detection.dto;

import lombok.Data;

@Data
public class DetectionQuestionFavoriteListRequest {
    private String userId;
    private String questionTypeCode;
}
