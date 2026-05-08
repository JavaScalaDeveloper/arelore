package com.arelore.server.core.detection.dto;

import lombok.Data;

import java.util.List;

@Data
public class DetectionQuestionFavoritePageResponse {
    private long total;
    private int pageNum;
    private int pageSize;
    private List<DetectionQuestionFavoriteItemResponse> records;
}
