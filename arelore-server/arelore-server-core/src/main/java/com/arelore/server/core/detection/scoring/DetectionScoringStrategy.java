package com.arelore.server.core.detection.scoring;

import com.alibaba.fastjson2.JSONObject;
import com.arelore.server.core.detection.entity.UserDetectionType;

import java.util.Map;

/**
 * 检测评分策略接口。
 * 不同测评类型可配置不同 resultMode，并由对应策略计算结果。
 */
public interface DetectionScoringStrategy {
    /**
     * 策略支持的评分模式，如 pair_compare / max_score。
     */
    String mode();

    /**
     * 根据评分配置与维度分数计算最终结果字符串。
     */
    String calculate(
        UserDetectionType detectionType,
        JSONObject scoringConfig,
        Map<String, Integer> scoreByDimension
    );
}

