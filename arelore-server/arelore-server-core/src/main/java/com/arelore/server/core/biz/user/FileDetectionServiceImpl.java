package com.arelore.server.core.biz.user;

import cn.hutool.core.text.CharSequenceUtil;
import com.arelore.server.core.biz.user.dto.FileDetectionResponse;
import com.arelore.server.core.biz.user.dto.TextMaskRequest;
import com.arelore.server.core.biz.user.dto.TextMaskResponse;
import com.arelore.server.core.biz.user.factory.FileProcessingStrategyFactory;
import com.arelore.server.core.biz.user.factory.SensitiveMaskStrategyFactory;
import com.arelore.server.core.biz.user.FileDetectionService;
import com.arelore.server.core.biz.user.TextMaskService;
import com.arelore.server.core.biz.user.strategy.FileProcessingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件检测服务实现类
 */
@Slf4j
@Service
public class FileDetectionServiceImpl implements FileDetectionService {
    
    @Autowired
    private FileProcessingStrategyFactory fileProcessingStrategyFactory;
    
    @Autowired
    private TextMaskService textMaskService;
    
    @Autowired
    private SensitiveMaskStrategyFactory sensitiveMaskStrategyFactory;
    
    @Override
    public FileDetectionResponse detectFile(MultipartFile file, List<String> sensitiveTypes) {
        log.info("开始文件检测，文件名：{}，敏感类型：{}", file.getOriginalFilename(), sensitiveTypes);
        
        try {
            // 使用策略模式处理文件，提取文本内容
            FileProcessingStrategy strategy = fileProcessingStrategyFactory.getStrategy(file);
            String content = strategy.processFile(file);
            
            log.info("文件内容提取完成，内容长度：{}", content.length());
            
            // 如果提取的内容为空，直接返回空结果
            if (CharSequenceUtil.isBlank(content)) {
                return FileDetectionResponse.builder()
                    .detectionResult("")
                    .processedCount(0)
                    .usedTypes(new ArrayList<>())
                    .sensitiveTypeInfoList(new ArrayList<>())
                    .build();
            }
            
            // 调用文本脱敏服务进行敏感信息检测
            TextMaskRequest maskRequest = new TextMaskRequest();
            maskRequest.setText(content);
            maskRequest.setSensitiveTypes(sensitiveTypes);
            maskRequest.setMaskMethod("keep_partially");
            TextMaskResponse maskResponse = textMaskService.maskText(maskRequest);
            
            // 构建文件检测响应
            List<FileDetectionResponse.SensitiveTypeInfo> sensitiveTypeInfoList = new ArrayList<>();
            for (TextMaskResponse.SensitiveTypeInfo info : maskResponse.getSensitiveTypeInfoList()) {
                sensitiveTypeInfoList.add(FileDetectionResponse.SensitiveTypeInfo.builder()
                    .code(info.getCode())
                    .label(info.getLabel())
                    .example(info.getExample())
                    .build());
            }
            
            FileDetectionResponse response = FileDetectionResponse.builder()
                .detectionResult(content)
                .processedCount(content.length())
                .usedTypes(maskResponse.getUsedTypes())
                .sensitiveTypeInfoList(sensitiveTypeInfoList)
                .build();
            
            log.info("文件检测完成，处理字符数：{}，使用策略数：{}", 
                response.getProcessedCount(), response.getUsedTypes().size());
            
            return response;
            
        } catch (Exception e) {
            log.error("文件检测失败", e);
            throw new RuntimeException("文件检测失败：" + e.getMessage());
        }
    }
}
