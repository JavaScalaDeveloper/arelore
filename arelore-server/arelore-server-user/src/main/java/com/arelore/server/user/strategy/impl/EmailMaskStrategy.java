package com.arelore.server.user.strategy.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.DesensitizedUtil;
import com.arelore.server.user.strategy.SensitiveMaskStrategy;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 邮箱脱敏策略
 */
@Component
public class EmailMaskStrategy implements SensitiveMaskStrategy {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    );
    
    private String matchedExample;
    
    @Override
    public String mask(String text) {
        if (CharSequenceUtil.isBlank(text)) {
            matchedExample = null;
            return text;
        }
        
        matchedExample = null;
        // 使用正则替换邮箱信息，保留前 3 个字符和域名，中间用*代替
        return EMAIL_PATTERN.matcher(text).replaceAll(match -> {
            String email = match.group();
            if (matchedExample == null) {
                matchedExample = email;
            }
            return DesensitizedUtil.email(email);
        });
    }
    
    @Override
    public String getType() {
        return "email";
    }
    
    @Override
    public String getExample() {
        return matchedExample;
    }
}
