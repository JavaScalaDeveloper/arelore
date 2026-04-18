package com.arelore.server.core.user.strategy.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.arelore.server.core.user.strategy.SensitiveMaskStrategy;
import org.springframework.stereotype.Component;

/**
 * 全量脱敏策略
 */
@Component
public class FullMaskStrategy implements SensitiveMaskStrategy {
    
    @Override
    public String mask(String text) {
        if (CharSequenceUtil.isBlank(text)) {
            return text;
        }
        
        // 将所有字符替换为*
        return "*".repeat(text.length());
    }
    
    @Override
    public String getType() {
        return "full_mask";
    }
    
    @Override
    public String getExample() {
        return null;
    }
}
