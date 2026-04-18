package com.arelore.server.core.user.strategy.impl;

import com.arelore.server.core.user.strategy.FileProcessingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

/**
 * HTML文件处理策略
 */
@Slf4j
@Component
public class HtmlFileProcessingStrategy implements FileProcessingStrategy {
    
    private static final int MAX_CONTENT_LENGTH = 10000; // 最大提取字数
    
    // 正则表达式：匹配HTML标签
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    
    @Override
    public boolean supports(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (contentType.equals("text/html") 
            || file.getOriginalFilename().endsWith(".html") 
            || file.getOriginalFilename().endsWith(".htm"));
    }
    
    @Override
    public String processFile(MultipartFile file) throws Exception {
        log.info("开始处理HTML文件：{}", file.getOriginalFilename());
        
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        // 去除HTML标签，提取纯文本
        String textContent = HTML_TAG_PATTERN.matcher(content.toString()).replaceAll("");
        
        // 限制提取字数
        if (textContent.length() > MAX_CONTENT_LENGTH) {
            textContent = textContent.substring(0, MAX_CONTENT_LENGTH);
            log.info("HTML内容过长，已截断至{}字符", MAX_CONTENT_LENGTH);
        }
        
        log.info("HTML文件处理完成，提取内容长度：{}", textContent.length());
        return textContent;
    }
    
    @Override
    public String getType() {
        return "html";
    }
}