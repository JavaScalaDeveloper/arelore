package com.arelore.server.user.dto;

import lombok.Data;

import java.util.List;

/**
 * 文本脱敏请求 DTO
 */
@Data
public class TextMaskRequest {
    
    /**
     * 待脱敏的文本
     */
    private String text;
    
    /**
     * 敏感类型列表
     */
    private List<String> sensitiveTypes;
    
    /**
     * 脱敏方式：full_mask-全部替换，keep_partially-保留部分
     */
    private String maskMethod;
}
