package com.arelore.server.core.user.dto;

import lombok.Data;

@Data
public class CommonBinaryFileByHashRequest {
    /**
     * SHA-256 的 64 位十六进制字符串（小写或大写均可）
     */
    private String hashValue;
}

