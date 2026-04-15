package com.arelore.server.user.service;

import com.arelore.server.user.dto.FileDetectionRequest;
import com.arelore.server.user.dto.FileDetectionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件检测服务
 */
public interface FileDetectionService {
    
    /**
     * 检测文件中的敏感信息
     * 
     * @param file 文件
     * @param sensitiveTypes 敏感信息类型列表
     * @return 检测结果
     */
    FileDetectionResponse detectFile(MultipartFile file, List<String> sensitiveTypes);
}
