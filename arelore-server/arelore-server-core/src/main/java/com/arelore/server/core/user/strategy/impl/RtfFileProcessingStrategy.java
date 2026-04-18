package com.arelore.server.core.user.strategy.impl;

import com.arelore.server.core.user.strategy.FileProcessingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

/**
 * RTF文件处理策略
 */
@Slf4j
@Component
public class RtfFileProcessingStrategy implements FileProcessingStrategy {
    
    private static final int MAX_CONTENT_LENGTH = 10000; // 最大提取字数
    
    // 正则表达式：匹配RTF控制字和控制符号
    private static final Pattern RTF_CONTROL_PATTERN = Pattern.compile("\\\\[a-zA-Z]+[0-9]*|\\\\[\\^'\"\\\\{}]");
    
    @Override
    public boolean supports(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        return contentType != null && contentType.equals("application/rtf") 
            || filename != null && filename.endsWith(".rtf");
    }
    
    @Override
    public String processFile(MultipartFile file) throws Exception {
        log.info("开始处理RTF文件：{}", file.getOriginalFilename());
        
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        // 去除RTF控制字和控制符号，提取纯文本
        String textContent = RTF_CONTROL_PATTERN.matcher(content.toString()).replaceAll("");
        
        // 去除多余的空格和换行
        textContent = textContent.replaceAll("\\s+", " ").trim();
        
        // 限制提取字数
        if (textContent.length() > MAX_CONTENT_LENGTH) {
            textContent = textContent.substring(0, MAX_CONTENT_LENGTH);
            log.info("RTF内容过长，已截断至{}字符", MAX_CONTENT_LENGTH);
        }
        
        log.info("RTF文件处理完成，提取内容长度：{}", textContent.length());
        return textContent;
    }
    
    @Override
    public String getType() {
        return "rtf";
    }
}