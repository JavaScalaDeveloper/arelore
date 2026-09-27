package com.arelore.server.core.biz.user.strategy.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.arelore.server.core.biz.user.strategy.SensitiveMaskStrategy;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 车牌号脱敏策略
 */
@Component
public class LicensePlateMaskStrategy implements SensitiveMaskStrategy {
    
    private static final Pattern LICENSE_PLATE_PATTERN = Pattern.compile(
        "[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-Z0-9]{4,5}[A-Z0-9挂学警港澳]|[A-Z]{2}[A-Z0-9]{5,6}"
    );
    
    private String matchedExample;
    
    @Override
    public String mask(String text) {
        if (CharSequenceUtil.isBlank(text)) {
            matchedExample = null;
            return text;
        }
        
        matchedExample = null;
        // 使用正则匹配车牌号，保留省份简称和第一个字母
        return LICENSE_PLATE_PATTERN.matcher(text).replaceAll(match -> {
            String plate = match.group();
            if (matchedExample == null) {
                matchedExample = plate;
            }
            int length = plate.length();
            
            if (length <= 3) {
                return "*".repeat(length);
            }
            
            // 保留前3位（省份简称+第一个字母）
            int keepStart = 3;
            
            if (length <= keepStart + 2) {
                return plate.substring(0, keepStart) + "*".repeat(length - keepStart);
            }
            
            // 保留前3位和后1位
            return plate.substring(0, keepStart) + "*".repeat(length - keepStart - 1) + plate.substring(length - 1);
        });
    }
    
    @Override
    public String getType() {
        return "license_plate";
    }
    
    @Override
    public String getExample() {
        return matchedExample;
    }
}
