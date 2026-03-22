package com.arelore.server.user.strategy.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.arelore.server.user.strategy.SensitiveMaskStrategy;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 地址脱敏策略
 */
@Component
public class AddressMaskStrategy implements SensitiveMaskStrategy {
    
    private static final Pattern ADDRESS_PATTERN = Pattern.compile(
        "[省市县][\\u4e00-\\u9fa5 ]{2,30}(?:区|镇|乡|街道|路|道|巷|胡同|街)(?:\\d+[号号楼栋]?)?"
    );
    
    @Override
    public String mask(String text) {
        if (CharSequenceUtil.isBlank(text)) {
            return text;
        }
        
        // 使用正则替换地址信息，保留前 3 个字符，其余用*代替
        return ADDRESS_PATTERN.matcher(text).replaceAll(match -> {
            String address = match.group();
            if (address.length() <= 3) {
                return "*".repeat(address.length());
            }
            return address.substring(0, 3) + "*".repeat(address.length() - 3);
        });
    }
    
    @Override
    public String getType() {
        return "address";
    }
}
