package com.arelore.server.user.strategy.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.arelore.server.user.strategy.SensitiveMaskStrategy;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * IP 地址脱敏策略
 */
@Component
public class IpAddressMaskStrategy implements SensitiveMaskStrategy {
    
    private static final Pattern IP_PATTERN = Pattern.compile(
        "\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b"
    );
    
    @Override
    public String mask(String text) {
        if (CharSequenceUtil.isBlank(text)) {
            return text;
        }
        
        // 使用正则匹配IP地址，只保留第一段
        return IP_PATTERN.matcher(text).replaceAll(match -> {
            String ip = match.group();
            String[] parts = ip.split("\\.");
            
            if (parts.length != 4) {
                return ip;
            }
            
            // 保留第一段，其他段用*代替
            return parts[0] + ".***.***.***";
        });
    }
    
    @Override
    public String getType() {
        return "ip_address";
    }
}
