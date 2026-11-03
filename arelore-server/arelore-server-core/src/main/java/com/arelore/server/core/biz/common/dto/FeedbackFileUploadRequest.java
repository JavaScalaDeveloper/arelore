package com.arelore.server.core.biz.common.dto;

import lombok.Data;

@Data
public class FeedbackFileUploadRequest {
    /** 文件 base64（可带 data:xxx;base64, 前缀） */
    private String contentBase64;
    private String fileName;
    private String contentType;
}
