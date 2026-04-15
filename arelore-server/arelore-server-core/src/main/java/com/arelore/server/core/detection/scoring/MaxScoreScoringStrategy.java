package com.arelore.server.core.detection.scoring;

import com.alibaba.fastjson2.JSONObject;
import com.arelore.server.core.detection.entity.UserDetectionType;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;

@Component
public class MaxScoreScoringStrategy implements DetectionScoringStrategy {
    @Override
    public String mode() {
        return "max_score";
    }

    @Override
    public String calculate(UserDetectionType detectionType, JSONObject scoringConfig, Map<String, Integer> scoreByDimension) {
        if (scoreByDimension.isEmpty()) {
            throw new IllegalArgumentException("未命中任何维度分值");
        }
        return scoreByDimension.entrySet().stream()
            .max(Comparator.comparingInt(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .orElseThrow(() -> new IllegalArgumentException("评分失败"));
    }
}

