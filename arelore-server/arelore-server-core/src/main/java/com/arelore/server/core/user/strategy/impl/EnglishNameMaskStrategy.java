package com.arelore.server.core.user.strategy.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.arelore.server.core.user.strategy.SensitiveMaskStrategy;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 英文姓名脱敏策略
 */
@Component
public class EnglishNameMaskStrategy implements SensitiveMaskStrategy {
    
    private static final Pattern ENGLISH_NAME_PATTERN = Pattern.compile(
        "\\b[A-Z][a-z]+(?:\\s+[A-Z][a-z]+)*\\b"
    );
    
    private String matchedExample;
    
    @Override
    public String mask(String text) {
        if (CharSequenceUtil.isBlank(text)) {
            matchedExample = null;
            return text;
        }
        
        matchedExample = null;
        // 使用正则匹配英文姓名，只保留首字母
        return ENGLISH_NAME_PATTERN.matcher(text).replaceAll(match -> {
            String name = match.group();
            if (matchedExample == null) {
                matchedExample = name;
            }
            String[] parts = name.split("\\s+");
            
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].length() > 0) {
                    result.append(parts[i].charAt(0));
                    if (parts[i].length() > 1) {
                        result.append("*".repeat(parts[i].length() - 1));
                    }
                }
                if (i < parts.length - 1) {
                    result.append(" ");
                }
            }
            return result.toString();
        });
    }
    
    @Override
    public String getType() {
        return "english_name";
    }
    
    @Override
    public String getExample() {
        return matchedExample;
    }
}
