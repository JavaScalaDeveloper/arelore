package com.arelore.server.user.strategy.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.arelore.server.user.strategy.SensitiveMaskStrategy;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 汉语姓名脱敏策略
 */
@Component
public class ChineseNameMaskStrategy implements SensitiveMaskStrategy {
    
    private static final Pattern CHINESE_NAME_PATTERN = Pattern.compile(
        "[\\u4e00-\\u9fa5]{2,4}"
    );
    
    @Override
    public String mask(String text) {
        if (CharSequenceUtil.isBlank(text)) {
            return text;
        }
        
        // 替换中文姓名，只显示第一个字，后面用*代替
        return CHINESE_NAME_PATTERN.matcher(text).replaceAll(match -> {
            String name = match.group();
            if (name.length() <= 1) {
                return "*";
            }
            return name.charAt(0) + "*".repeat(name.length() - 1);
        });
    }
    
    @Override
    public String getType() {
        return "chinese_name";
    }
}
