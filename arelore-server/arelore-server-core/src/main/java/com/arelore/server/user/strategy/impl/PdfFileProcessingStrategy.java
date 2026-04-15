package com.arelore.server.user.strategy.impl;

import com.arelore.server.user.strategy.FileProcessingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * PDF文件处理策略
 */
@Slf4j
@Component
public class PdfFileProcessingStrategy implements FileProcessingStrategy {
    
    private static final int MAX_CONTENT_LENGTH = 10000; // 最大提取字数
    
    @Override
    public boolean supports(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        return "application/pdf".equals(contentType) || 
               (originalFilename != null && originalFilename.endsWith(".pdf"));
    }
    
    @Override
    public String processFile(MultipartFile file) throws Exception {
        log.info("开始处理PDF文件：{}", file.getOriginalFilename());
        
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {
            
            PDFTextStripper stripper = new PDFTextStripper();
            String content = stripper.getText(document);
            
            // 限制提取字数
            if (content.length() > MAX_CONTENT_LENGTH) {
                content = content.substring(0, MAX_CONTENT_LENGTH);
                log.info("PDF文件内容过长，已截断至{}字符", MAX_CONTENT_LENGTH);
            }
            
            log.info("PDF文件处理完成，提取内容长度：{}", content.length());
            return content;
        }
    }
    
    @Override
    public String getType() {
        return "pdf";
    }
}
