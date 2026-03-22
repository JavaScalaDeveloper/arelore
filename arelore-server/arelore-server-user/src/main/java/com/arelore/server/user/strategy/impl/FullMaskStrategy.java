package com.arelore.server.user.strategy.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.arelore.server.user.strategy.SensitiveMaskStrategy;
import org.springframework.stereotype.Component;

/**
 * 全量替换脱敏策略（全部用*代替）
 */
@Component
public class FullMaskStrategy implements SensitiveMaskStrategy {
    
    @Override
    public String mask(String text) {
        if (CharSequenceUtil.isBlank(text)) {
            return text;
        }
        // 全部替换为*
        return "*".repeat(text.length());
    }
    
    @Override
    public String getType() {
        return "full_mask";
    }
}
