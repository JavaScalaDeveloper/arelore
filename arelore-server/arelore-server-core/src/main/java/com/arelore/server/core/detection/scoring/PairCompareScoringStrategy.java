package com.arelore.server.core.detection.scoring;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.arelore.server.core.detection.entity.UserDetectionType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class PairCompareScoringStrategy implements DetectionScoringStrategy {
    @Override
    public String mode() {
        return "pair_compare";
    }

    @Override
    public String calculate(UserDetectionType detectionType, JSONObject scoringConfig, Map<String, Integer> scoreByDimension) {
        List<PairRule> pairRules = readPairRules(detectionType, scoringConfig);
        String tieBreaker = scoringConfig == null ? "left" : scoringConfig.getString("tieBreaker");
        if (tieBreaker == null || tieBreaker.isBlank()) {
            tieBreaker = "left";
        }

        StringBuilder result = new StringBuilder();
        for (PairRule pair : pairRules) {
            int leftScore = scoreByDimension.getOrDefault(pair.left, 0);
            int rightScore = scoreByDimension.getOrDefault(pair.right, 0);
            if (leftScore > rightScore) {
                result.append(pair.left);
            } else if (leftScore < rightScore) {
                result.append(pair.right);
            } else {
                result.append("right".equalsIgnoreCase(tieBreaker) ? pair.right : pair.left);
            }
        }
        return result.toString();
    }

    private List<PairRule> readPairRules(UserDetectionType type, JSONObject scoringConfig) {
        List<PairRule> rules = new ArrayList<>();
        JSONArray pairArray = scoringConfig == null ? null : scoringConfig.getJSONArray("pairs");
        if (pairArray != null) {
            for (int i = 0; i < pairArray.size(); i++) {
                JSONObject obj = pairArray.getJSONObject(i);
                if (obj == null) {
                    continue;
                }
                String left = obj.getString("left");
                String right = obj.getString("right");
                if (left != null && !left.isBlank() && right != null && !right.isBlank()) {
                    rules.add(new PairRule(left, right));
                }
            }
        }
        if (!rules.isEmpty()) {
            return rules;
        }
        if ("MBTI".equalsIgnoreCase(type.getTypeCode())) {
            rules.add(new PairRule("E", "I"));
            rules.add(new PairRule("S", "N"));
            rules.add(new PairRule("T", "F"));
            rules.add(new PairRule("J", "P"));
            return rules;
        }
        throw new IllegalArgumentException("pair_compare 模式缺少 pairs 配置");
    }

    private static class PairRule {
        private final String left;
        private final String right;

        private PairRule(String left, String right) {
            this.left = left;
            this.right = right;
        }
    }
}

