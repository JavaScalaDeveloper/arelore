package com.arelore.server.core.detection.dto;

import lombok.Data;

@Data
public class DetectionQuestionFavoriteToggleRequest {
    private String userId;
    private String questionTypeCode;
    private String questionCode;
    private Boolean favorite;
    private String extraInfo;
}
