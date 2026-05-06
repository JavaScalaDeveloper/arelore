package com.arelore.server.core.detection.scoring;

import com.alibaba.fastjson2.JSONObject;
import com.arelore.server.core.detection.entity.UserDetectionType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 客观题累计得分：对指定维度（默认 {@code score}）求和，并可与满分拼接为 {@code 得分/满分}。
 * 用于软考等单选题库：每题正确选项配置 {@code "scores":{"score":1}}，错误为 0 或省略。
 */
@Component
public class ObjectiveSumScoringStrategy implements DetectionScoringStrategy {
    @Override
    public String mode() {
        return "objective_sum";
    }

    @Override
    public String calculate(UserDetectionType detectionType, JSONObject scoringConfig, Map<String, Integer> scoreByDimension) {
        String dim = "score";
        if (scoringConfig != null && StringUtils.hasText(scoringConfig.getString("scoreDimension"))) {
            dim = scoringConfig.getString("scoreDimension");
        }
        int sum = scoreByDimension.getOrDefault(dim, 0);
        if (scoringConfig != null) {
            Integer maxScore = scoringConfig.getInteger("maxScore");
            if (maxScore != null && maxScore > 0) {
                return sum + "/" + maxScore;
            }
        }
        return String.valueOf(sum);
    }
}
