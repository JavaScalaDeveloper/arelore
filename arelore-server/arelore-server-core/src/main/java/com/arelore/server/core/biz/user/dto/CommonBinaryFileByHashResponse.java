package com.arelore.server.core.biz.user.dto;

import lombok.Data;

@Data
public class CommonBinaryFileByHashResponse {
    private String hashValue;
    private String contentType;
    private String base64Data;
}

