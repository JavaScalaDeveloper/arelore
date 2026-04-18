package com.arelore.server.core.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 文件检测响应DTO
 */
@Data
@Builder
public class FileDetectionResponse {
    
    /**
     * 检测结果文本（提取的文件内容）
     */
    private String detectionResult;
    
    /**
     * 检测到的敏感类型信息列表
     */
    private List<SensitiveTypeInfo> sensitiveTypeInfoList;
    
    /**
     * 处理的字符数
     */
    private Integer processedCount;
    
    /**
     * 使用的敏感类型列表
     */
    private List<String> usedTypes;
    
    /**
     * 敏感类型信息
     */
    @Data
    @Builder
    public static class SensitiveTypeInfo {
        
        /**
         * 敏感类型编码
         */
        private String code;
        
        /**
         * 敏感类型名称
         */
        private String label;
        
        /**
         * 示例值
         */
        private String example;
    }
}
