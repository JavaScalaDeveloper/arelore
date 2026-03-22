package com.arelore.server.user.strategy.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.DesensitizedUtil;
import com.arelore.server.user.strategy.SensitiveMaskStrategy;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 中国手机号脱敏策略
 */
@Component
public class ChinaPhoneMaskStrategy implements SensitiveMaskStrategy {
    
    private static final Pattern PHONE_PATTERN = Pattern.compile("1[3-9]\\d{9}");
    
    @Override
    public String mask(String text) {
        if (CharSequenceUtil.isBlank(text)) {
            return text;
        }
        
        // 使用正则匹配手机号，保留前 3 后 4，中间用*代替
        return PHONE_PATTERN.matcher(text).replaceAll(match -> {
            String phone = match.group();
            return DesensitizedUtil.mobilePhone(phone);
        });
    }
    
    @Override
    public String getType() {
        return "china_phone";
    }
}
