package com.arelore.server.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文本脱敏响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextMaskResponse {
    
    /**
     * 脱敏后的文本
     */
    private String maskedText;
    
    /**
     * 处理的字符数
     */
    private Integer processedCount;
    
    /**
     * 使用的敏感类型列表
     */
    private java.util.List<String> usedTypes;
    
    /**
     * 敏感类型及样例列表
     */
    private java.util.List<SensitiveTypeInfo> sensitiveTypeInfoList;
    
    /**
     * 敏感类型信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SensitiveTypeInfo {
        /**
         * 类型代码
         */
        private String code;
        
        /**
         * 类型名称
         */
        private String label;
        
        /**
         * 样例文本
         */
        private String example;
    }
}
