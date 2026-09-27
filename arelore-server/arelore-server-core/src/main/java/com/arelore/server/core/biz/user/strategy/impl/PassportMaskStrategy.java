package com.arelore.server.core.biz.user.strategy.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.arelore.server.core.biz.user.strategy.SensitiveMaskStrategy;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 护照号码脱敏策略
 */
@Component
public class PassportMaskStrategy implements SensitiveMaskStrategy {
    
    private static final Pattern PASSPORT_PATTERN = Pattern.compile(
        "\\b[A-Z]{1,2}[0-9]{6,9}\\b|\\b[EP][0-9]{8}\\b"
    );
    
    private String matchedExample;
    
    @Override
    public String mask(String text) {
        if (CharSequenceUtil.isBlank(text)) {
            matchedExample = null;
            return text;
        }
        
        matchedExample = null;
        // 使用正则匹配护照号码，保留前 2 位和后 3 位
        return PASSPORT_PATTERN.matcher(text).replaceAll(match -> {
            String passport = match.group();
            if (matchedExample == null) {
                matchedExample = passport;
            }
            int length = passport.length();
            
            if (length <= 5) {
                return "*".repeat(length);
            }
            
            int keepStart = 2;
            int keepEnd = 3;
            
            if (length <= keepStart + keepEnd) {
                return "*".repeat(length);
            }
            
            return passport.substring(0, keepStart) + "*".repeat(length - keepStart - keepEnd) + passport.substring(length - keepEnd);
        });
    }
    
    @Override
    public String getType() {
        return "passport";
    }
    
    @Override
    public String getExample() {
        return matchedExample;
    }
}
