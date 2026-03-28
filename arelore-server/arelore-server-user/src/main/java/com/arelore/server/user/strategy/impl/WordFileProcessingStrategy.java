package com.arelore.server.user.strategy.impl;

import com.arelore.server.user.strategy.FileProcessingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Word文档处理策略
 */
@Slf4j
@Component
public class WordFileProcessingStrategy implements FileProcessingStrategy {
    
    private static final int MAX_CONTENT_LENGTH = 10000; // 最大提取字数
    
    @Override
    public boolean supports(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        return "application/msword".equals(contentType) || 
               "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(contentType) ||
               (originalFilename != null && (originalFilename.endsWith(".doc") || originalFilename.endsWith(".docx")));
    }
    
    @Override
    public String processFile(MultipartFile file) throws Exception {
        log.info("开始处理Word文档：{}", file.getOriginalFilename());
        
        try (InputStream inputStream = file.getInputStream()) {
            String content;
            String originalFilename = file.getOriginalFilename();
            
            if (originalFilename != null && originalFilename.endsWith(".docx")) {
                // 处理.docx文件
                XWPFDocument document = new XWPFDocument(inputStream);
                XWPFWordExtractor extractor = new XWPFWordExtractor(document);
                content = extractor.getText();
                extractor.close();
            } else {
                // 处理.doc文件
                WordExtractor extractor = new WordExtractor(inputStream);
                content = extractor.getText();
                extractor.close();
            }
            
            // 限制提取字数
            if (content != null && content.length() > MAX_CONTENT_LENGTH) {
                content = content.substring(0, MAX_CONTENT_LENGTH);
                log.info("Word文档内容过长，已截断至{}字符", MAX_CONTENT_LENGTH);
            }
            
            log.info("Word文档处理完成，提取内容长度：{}", content != null ? content.length() : 0);
            return content != null ? content : "";
        }
    }
    
    @Override
    public String getType() {
        return "word";
    }
}
