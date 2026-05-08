package com.arelore.server.core.detection.dto;

import lombok.Data;

@Data
public class DetectionQuestionFavoritePageRequest {
    private String userId;
    /** 从 1 开始 */
    private Integer pageNum;
    private Integer pageSize;
}
