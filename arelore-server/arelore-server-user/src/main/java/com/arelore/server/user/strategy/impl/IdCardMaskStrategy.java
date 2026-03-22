package com.arelore.server.user.strategy.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.IdcardUtil;
import com.arelore.server.user.strategy.SensitiveMaskStrategy;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 身份证号脱敏策略
 */
@Component
public class IdCardMaskStrategy implements SensitiveMaskStrategy {
    
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("\\b[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]\\b");
    
    @Override
    public String mask(String text) {
        if (CharSequenceUtil.isBlank(text)) {
            return text;
        }
        
        // 使用正则匹配身份证号，保留前 6 后 4
        return ID_CARD_PATTERN.matcher(text).replaceAll(match -> {
            String idCard = match.group();
            String result = DesensitizedUtil.idCardNum(idCard, 6, 4);
            return result != null ? result : idCard;
        });
    }
    
    @Override
    public String getType() {
        return "id_card";
    }
}
