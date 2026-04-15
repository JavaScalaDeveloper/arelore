package com.arelore.server.user.strategy;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件处理策略接口
 */
public interface FileProcessingStrategy {
    
    /**
     * 判断是否支持该文件类型
     * 
     * @param file 文件
     * @return 是否支持
     */
    boolean supports(MultipartFile file);
    
    /**
     * 处理文件，提取文本内容
     * 
     * @param file 文件
     * @return 提取的文本内容
     * @throws Exception 处理异常
     */
    String processFile(MultipartFile file) throws Exception;
    
    /**
     * 获取策略类型
     * 
     * @return 策略类型
     */
    String getType();
}
