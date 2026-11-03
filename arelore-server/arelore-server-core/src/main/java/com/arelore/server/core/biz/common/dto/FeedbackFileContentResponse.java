package com.arelore.server.core.biz.common.dto;

import lombok.Data;

@Data
public class FeedbackFileContentResponse {
    private Long fileId;
    private String contentType;
    private String contentBase64;
}
