package com.arelore.server.core.biz.word.admin.dto;

import lombok.Data;

@Data
public class AdminWordPictureContentResponse {
    private Long fileId;
    private String contentType;
    private String base64Data;
}
