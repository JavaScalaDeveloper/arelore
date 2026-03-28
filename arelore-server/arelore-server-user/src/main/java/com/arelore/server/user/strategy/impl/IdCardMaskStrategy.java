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
    
    private static final Pattern ID_CARD_PATTERN = Pattern.compile(
        "[1-9]\\d{5}(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]"
    );
    
    private String matchedExample;
    
    @Override
    public String mask(String text) {
        if (CharSequenceUtil.isBlank(text)) {
            matchedExample = null;
            return text;
        }
        
        matchedExample = null;
        // 使用正则替换身份证号信息，保留前 6 后 4，中间用*代替
        return ID_CARD_PATTERN.matcher(text).replaceAll(match -> {
            String idCard = match.group();
            if (matchedExample == null) {
                matchedExample = idCard;
            }
            return DesensitizedUtil.idCardNum(idCard, 6, 4);
        });
    }
    
    @Override
    public String getType() {
        return "id_card";
    }
    
    @Override
    public String getExample() {
        return matchedExample;
    }
}
