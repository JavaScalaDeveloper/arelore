package com.arelore.server.user.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.arelore.server.user.dto.TextMaskRequest;
import com.arelore.server.user.dto.TextMaskResponse;
import com.arelore.server.user.enums.SensitiveType;
import com.arelore.server.user.factory.SensitiveMaskStrategyFactory;
import com.arelore.server.user.service.TextMaskService;
import com.arelore.server.user.strategy.SensitiveMaskStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本脱敏服务实现类
 */
@Slf4j
@Service
public class TextMaskServiceImpl implements TextMaskService {
    
    @Autowired
    private SensitiveMaskStrategyFactory strategyFactory;
    
    @Override
    public TextMaskResponse maskText(TextMaskRequest request) {
        log.info("开始执行文本脱敏，敏感类型：{}, 脱敏方式：{}", 
            request.getSensitiveTypes(), request.getMaskMethod());
        
        // 参数校验
        if (CharSequenceUtil.isBlank(request.getText())) {
            return TextMaskResponse.builder()
                .maskedText("")
                .processedCount(0)
                .usedTypes(new ArrayList<>())
                .build();
        }
        
        if (request.getSensitiveTypes() == null || request.getSensitiveTypes().isEmpty()) {
            throw new IllegalArgumentException("请至少选择一种敏感类型");
        }
        
        String result = request.getText();
        List<String> usedTypes = new ArrayList<>();
        List<String> matchedTypes = new ArrayList<>(); // 实际匹配到的类型
        
        try {
            // 依次应用每种敏感类型的脱敏策略
            for (String typeCode : request.getSensitiveTypes()) {
                // 跳过自定义正则表达式（暂不支持）
                if (SensitiveType.CUSTOM_REGEX.getCode().equals(typeCode)) {
                    log.warn("自定义正则表达式暂不支持，已跳过");
                    continue;
                }
                
                // 验证类型是否有效
                SensitiveType.fromCode(typeCode);
                
                // 获取对应的脱敏策略
                SensitiveMaskStrategy strategy = strategyFactory.getStrategy(typeCode);
                if (strategy != null) {
                    String oldResult = result;
                    result = strategy.mask(result);
                    // 检查是否实际进行了脱敏（结果发生变化）
                    if (!oldResult.equals(result)) {
                        usedTypes.add(typeCode);
                        matchedTypes.add(typeCode);
                        log.debug("应用脱敏策略：{} 成功，发现匹配", typeCode);
                    } else {
                        log.debug("应用脱敏策略：{} 成功，但未发现匹配", typeCode);
                    }
                }
            }
            
            // 构建敏感类型信息列表（只包含实际匹配到的类型）
            List<TextMaskResponse.SensitiveTypeInfo> sensitiveTypeInfoList = new ArrayList<>();
            for (String typeCode : matchedTypes) {
                try {
                    SensitiveType type = SensitiveType.fromCode(typeCode);
                    TextMaskResponse.SensitiveTypeInfo info = TextMaskResponse.SensitiveTypeInfo.builder()
                        .code(type.getCode())
                        .label(type.getName())
                        .example(type.getExample())
                        .build();
                    sensitiveTypeInfoList.add(info);
                } catch (IllegalArgumentException e) {
                    log.warn("未知的敏感类型：{}", typeCode);
                }
            }
            
            TextMaskResponse response = TextMaskResponse.builder()
                .maskedText(result)
                .processedCount(request.getText().length())
                .usedTypes(matchedTypes) // 只返回实际匹配到的类型
                .sensitiveTypeInfoList(sensitiveTypeInfoList)
                .build();
            
            log.info("文本脱敏完成，处理字符数：{}, 使用策略数：{}", 
                response.getProcessedCount(), response.getUsedTypes().size());
            
            return response;
            
        } catch (Exception e) {
            log.error("文本脱敏失败", e);
            throw new RuntimeException("文本脱敏失败：" + e.getMessage());
        }
    }
}
