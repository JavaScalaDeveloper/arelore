package com.arelore.server.core.user.strategy.impl;

import com.arelore.server.core.user.strategy.FileProcessingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * PPT文件处理策略
 */
@Slf4j
@Component
public class PptFileProcessingStrategy implements FileProcessingStrategy {
    
    private static final int MAX_CONTENT_LENGTH = 10000; // 最大提取字数
    
    @Override
    public boolean supports(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        return "application/vnd.ms-powerpoint".equals(contentType) || 
               "application/vnd.openxmlformats-officedocument.presentationml.presentation".equals(contentType) ||
               (originalFilename != null && (originalFilename.endsWith(".ppt") || originalFilename.endsWith(".pptx")));
    }
    
    @Override
    public String processFile(MultipartFile file) throws Exception {
        log.info("开始处理PPT文件：{}", file.getOriginalFilename());
        
        try (InputStream inputStream = file.getInputStream()) {
            StringBuilder content = new StringBuilder();
            String originalFilename = file.getOriginalFilename();
            
            if (originalFilename != null && originalFilename.endsWith(".pptx")) {
                // 处理.pptx文件
                XMLSlideShow ppt = new XMLSlideShow(inputStream);
                List<XSLFSlide> slides = ppt.getSlides();
                
                for (XSLFSlide slide : slides) {
                    for (Object shapeObj : slide.getShapes()) {
                        if (shapeObj instanceof XSLFTextShape) {
                            XSLFTextShape shape = (XSLFTextShape) shapeObj;
                            content.append(shape.getText()).append("\n");
                        }
                    }
                }
            } else {
                // 处理.ppt文件
                HSLFSlideShow ppt = new HSLFSlideShow(inputStream);
                List<HSLFSlide> slides = ppt.getSlides();
                
                for (HSLFSlide slide : slides) {
                    List<List<HSLFTextParagraph>> paragraphsList = slide.getTextParagraphs();
                    for (List<HSLFTextParagraph> paragraphs : paragraphsList) {
                        for (HSLFTextParagraph paragraph : paragraphs) {
                            content.append(paragraph.getText(paragraphs)).append("\n");
                        }
                    }
                }
            }
            
            String result = content.toString();
            
            // 限制提取字数
            if (result.length() > MAX_CONTENT_LENGTH) {
                result = result.substring(0, MAX_CONTENT_LENGTH);
                log.info("PPT文件内容过长，已截断至{}字符", MAX_CONTENT_LENGTH);
            }
            
            log.info("PPT文件处理完成，提取内容长度：{}", result.length());
            return result;
        }
    }
    
    @Override
    public String getType() {
        return "ppt";
    }
}
