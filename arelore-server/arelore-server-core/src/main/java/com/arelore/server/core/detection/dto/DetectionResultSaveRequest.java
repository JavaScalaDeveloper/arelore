package com.arelore.server.core.detection.dto;

import lombok.Data;

import java.util.List;

@Data
public class DetectionResultSaveRequest {
    private String userId;
    private String userDetectTypeCode;
    private String userDetectResult;
    private String extraInfo;
    private List<AnsweredQuestion> answeredQuestions;

    @Data
    public static class AnsweredQuestion {
        private Long questionId;
        private String questionCode;
        private String selectedOptionKey;
    }
}
