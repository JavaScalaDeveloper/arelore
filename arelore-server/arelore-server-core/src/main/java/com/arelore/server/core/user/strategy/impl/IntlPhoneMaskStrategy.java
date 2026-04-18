package com.arelore.server.core.user.strategy.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.arelore.server.core.user.strategy.SensitiveMaskStrategy;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 国际手机号脱敏策略
 */
@Component
public class IntlPhoneMaskStrategy implements SensitiveMaskStrategy {
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "\\+[1-9]\\d{0,2}[- ]?\\(?\\d{2,4}\\)?[- ]?\\d{3,6}[- ]?\\d{3,6}"
    );
    
    private String matchedExample;
    
    @Override
    public String mask(String text) {
        if (CharSequenceUtil.isBlank(text)) {
            matchedExample = null;
            return text;
        }
        
        matchedExample = null;
        // 使用正则匹配国际手机号，保留前 3 位和后 4 位，中间用*代替
        return PHONE_PATTERN.matcher(text).replaceAll(match -> {
            String phone = match.group();
            if (matchedExample == null) {
                matchedExample = phone;
            }
            // 移除非数字字符以便计算
            String digits = phone.replaceAll("[^\\d]", "");
            
            if (digits.length() <= 7) {
                // 如果号码太短，只保留前3位
                return phone.substring(0, Math.min(3, phone.length())) + "*".repeat(Math.max(0, phone.length() - 3));
            }
            
            // 保留前3位和后4位
            int phoneLength = phone.length();
            int keepStart = 3;
            int keepEnd = 4;
            
            if (phoneLength <= keepStart + keepEnd) {
                return "*".repeat(phoneLength);
            }
            
            return phone.substring(0, keepStart) + "*".repeat(phoneLength - keepStart - keepEnd) + phone.substring(phoneLength - keepEnd);
        });
    }
    
    @Override
    public String getType() {
        return "intl_phone";
    }
    
    @Override
    public String getExample() {
        return matchedExample;
    }
}
