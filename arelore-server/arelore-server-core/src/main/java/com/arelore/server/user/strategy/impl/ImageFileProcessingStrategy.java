package com.arelore.server.user.strategy.impl;

import com.arelore.server.user.strategy.FileProcessingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * 图片文件处理策略（OCR识别）
 */
@Slf4j
@Component
public class ImageFileProcessingStrategy implements FileProcessingStrategy {
    
    private static final int MAX_CONTENT_LENGTH = 10000; // 最大提取字数
    
    @Override
    public boolean supports(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
    
    @Override
    public String processFile(MultipartFile file) throws Exception {
        log.info("开始处理图片文件：{}", file.getOriginalFilename());
        
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new RuntimeException("无法读取图片文件");
            }
            
            try {
                // 使用反射动态加载Tesseract，避免静态初始化错误
                Class<?> tesseractClass = Class.forName("net.sourceforge.tess4j.Tesseract");
                Object tesseract = tesseractClass.getDeclaredConstructor().newInstance();
                
                // 设置datapath
                java.lang.reflect.Method setDatapathMethod = tesseractClass.getMethod("setDatapath", String.class);
                setDatapathMethod.invoke(tesseract, "/usr/share/tesseract-ocr/4.00/tessdata");
                
                // 设置language
                java.lang.reflect.Method setLanguageMethod = tesseractClass.getMethod("setLanguage", String.class);
                setLanguageMethod.invoke(tesseract, "chi_sim+eng");
                
                // 调用doOCR方法
                java.lang.reflect.Method doOCRMethod = tesseractClass.getMethod("doOCR", java.awt.image.BufferedImage.class);
                String content = (String) doOCRMethod.invoke(tesseract, image);
                
                // 限制提取字数
                if (content.length() > MAX_CONTENT_LENGTH) {
                    content = content.substring(0, MAX_CONTENT_LENGTH);
                    log.info("OCR识别内容过长，已截断至{}字符", MAX_CONTENT_LENGTH);
                }
                
                log.info("图片OCR识别完成，提取内容长度：{}", content.length());
                return content;
            } catch (Exception e) {
                // 处理InvocationTargetException，获取真实的异常
                Throwable cause = e.getCause();
                if (cause != null) {
                    log.warn("Tesseract OCR调用失败，原因：{}", cause.getMessage());
                } else {
                    log.warn("Tesseract OCR不可用，使用降级处理", e);
                }
                // 返回降级内容
                return "[图片文件：" + file.getOriginalFilename() + "，大小：" + file.getSize() + "字节，无法进行OCR文字识别]";
            }
        }
    }
    
    @Override
    public String getType() {
        return "image";
    }
}
