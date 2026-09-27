package com.arelore.server.core.biz.user.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件检测请求DTO
 */
@Data
public class FileDetectionRequest {
    
    /**
     * 上传的文件
     */
    private MultipartFile file;
    
    /**
     * 敏感信息类型列表
     */
    private List<String> sensitiveTypes;
}
