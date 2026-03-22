package com.arelore.server.user.factory;

import com.arelore.server.user.strategy.SensitiveMaskStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 敏感信息脱敏策略工厂
 */
@Component
public class SensitiveMaskStrategyFactory {
    
    private final Map<String, SensitiveMaskStrategy> strategyMap = new ConcurrentHashMap<>();
    
    @Autowired
    private List<SensitiveMaskStrategy> strategies;
    
    @PostConstruct
    public void init() {
        // 注册所有策略
        for (SensitiveMaskStrategy strategy : strategies) {
            strategyMap.put(strategy.getType(), strategy);
        }
    }
    
    /**
     * 获取指定类型的脱敏策略
     * 
     * @param type 策略类型
     * @return 脱敏策略
     */
    public SensitiveMaskStrategy getStrategy(String type) {
        SensitiveMaskStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的脱敏策略类型：" + type);
        }
        return strategy;
    }
    
    /**
     * 判断是否支持该策略类型
     * 
     * @param type 策略类型
     * @return true-支持，false-不支持
     */
    public boolean supports(String type) {
        return strategyMap.containsKey(type);
    }
}
