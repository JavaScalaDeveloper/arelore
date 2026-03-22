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
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\b");
    
    @Override
    public String mask(String text) {
        if (CharSequenceUtil.isBlank(text)) {
            return text;
        }
        
        // 使用正则匹配邮箱，只显示首字母和@及域名
        return EMAIL_PATTERN.matcher(text).replaceAll(match -> {
            String email = match.group();
            return DesensitizedUtil.email(email);
        });
    }
    
    @Override
    public String getType() {
        return "email";
    }
}
