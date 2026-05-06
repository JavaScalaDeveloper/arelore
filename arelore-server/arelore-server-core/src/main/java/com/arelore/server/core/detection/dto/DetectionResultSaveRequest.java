package com.arelore.server.core.detection.dto;

import lombok.Data;

import java.util.List;

@Data
public class DetectionResultSaveRequest {
    private String userId;
    private String userDetectTypeCode;
    private String userDetectResult;
    private String extraInfo;
    /**
     * 是否交卷：
     * - false: 答题中保存进度（仅更新 extra_info）
     * - true: 交卷并计算最终结果
     */
    private Boolean submitPaper;
    private List<AnsweredQuestion> answeredQuestions;

    @Data
    public static class AnsweredQuestion {
        private Long questionId;
        private String questionCode;
        private String selectedOptionKey;
    }
}
