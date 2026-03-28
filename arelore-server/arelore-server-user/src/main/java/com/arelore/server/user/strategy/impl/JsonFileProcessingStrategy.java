package com.arelore.server.user.strategy.impl;

import com.arelore.server.user.strategy.FileProcessingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * JSON文件处理策略
 */
@Slf4j
@Component
public class JsonFileProcessingStrategy implements FileProcessingStrategy {
    
    private static final int MAX_CONTENT_LENGTH = 10000; // 最大提取字数
    
    @Override
    public boolean supports(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        return contentType != null && contentType.equals("application/json") 
            || filename != null && filename.endsWith(".json");
    }
    
    @Override
    public String processFile(MultipartFile file) throws Exception {
        log.info("开始处理JSON文件：{}", file.getOriginalFilename());
        
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        String textContent = content.toString();
        
        // 限制提取字数
        if (textContent.length() > MAX_CONTENT_LENGTH) {
            textContent = textContent.substring(0, MAX_CONTENT_LENGTH);
            log.info("JSON内容过长，已截断至{}字符", MAX_CONTENT_LENGTH);
        }
        
        log.info("JSON文件处理完成，提取内容长度：{}", textContent.length());
        return textContent;
    }
    
    @Override
    public String getType() {
        return "json";
    }
}