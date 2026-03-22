package com.arelore.server.user.strategy.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.DesensitizedUtil;
import com.arelore.server.user.strategy.SensitiveMaskStrategy;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 银行卡号脱敏策略
 */
@Component
public class BankCardMaskStrategy implements SensitiveMaskStrategy {
    
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("\\b\\d{13,19}\\b");
    
    @Override
    public String mask(String text) {
        if (CharSequenceUtil.isBlank(text)) {
            return text;
        }
        
        // 使用正则匹配银行卡号，保留前 4 后 4
        return BANK_CARD_PATTERN.matcher(text).replaceAll(match -> {
            String card = match.group();
            return DesensitizedUtil.bankCard(card);
        });
    }
    
    @Override
    public String getType() {
        return "bank_card";
    }
}
