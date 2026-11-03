package com.arelore.server.core.biz.common.dto;

import lombok.Data;

@Data
public class FeedbackFileUploadResponse {
    private Long fileId;
    private String fileName;
    private String contentType;
}
