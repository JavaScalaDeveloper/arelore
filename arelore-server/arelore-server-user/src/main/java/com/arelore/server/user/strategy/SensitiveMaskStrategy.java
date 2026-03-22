package com.arelore.server.user.strategy;

/**
 * 敏感信息脱敏策略接口
 */
public interface SensitiveMaskStrategy {
    
    /**
     * 脱敏处理
     * 
     * @param text 原始文本
     * @return 脱敏后的文本
     */
    String mask(String text);
    
    /**
     * 获取策略类型
     * 
     * @return 策略类型
     */
    String getType();
}
