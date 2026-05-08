package com.arelore.server.core.detection.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DetectionQuestionFavoriteItemResponse {
    private Long id;
    private LocalDateTime createTime;
    private String questionTypeCode;
    private String questionCode;
    /** 试卷名称（来自检测类型） */
    private String typeName;
    /** 题干摘要；题目不存在时为空 */
    private String questionName;
}
