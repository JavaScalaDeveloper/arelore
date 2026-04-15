package com.arelore.server.core.detection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.arelore.server.core.detection.dto.DetectionResultSaveRequest;
import com.arelore.server.core.detection.dto.DetectionResultSaveResponse;
import com.arelore.server.core.detection.entity.UserDetectResult;
import com.arelore.server.core.detection.entity.UserDetectResultHistory;
import com.arelore.server.core.detection.entity.UserDetectionQuestion;
import com.arelore.server.core.detection.entity.UserDetectionType;
import com.arelore.server.core.detection.mapper.UserDetectResultHistoryMapper;
import com.arelore.server.core.detection.mapper.UserDetectResultMapper;
import com.arelore.server.core.detection.mapper.UserDetectionQuestionMapper;
import com.arelore.server.core.detection.mapper.UserDetectionTypeMapper;
import com.arelore.server.core.detection.scoring.DetectionScoringStrategy;
import com.arelore.server.core.detection.service.UserDetectionResultService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserDetectionResultServiceImpl implements UserDetectionResultService {
    private final UserDetectResultMapper resultMapper;
    private final UserDetectResultHistoryMapper historyMapper;
    private final UserDetectionTypeMapper typeMapper;
    private final UserDetectionQuestionMapper questionMapper;
    private final Map<String, DetectionScoringStrategy> scoringStrategies;

    public UserDetectionResultServiceImpl(
        UserDetectResultMapper resultMapper,
        UserDetectResultHistoryMapper historyMapper,
        UserDetectionTypeMapper typeMapper,
        UserDetectionQuestionMapper questionMapper,
        List<DetectionScoringStrategy> scoringStrategyList
    ) {
        this.resultMapper = resultMapper;
        this.historyMapper = historyMapper;
        this.typeMapper = typeMapper;
        this.questionMapper = questionMapper;
        this.scoringStrategies = scoringStrategyList.stream()
            .collect(Collectors.toMap(DetectionScoringStrategy::mode, s -> s));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DetectionResultSaveResponse saveResultAndHistory(DetectionResultSaveRequest request) {
        if (request == null
            || !StringUtils.hasText(request.getUserId())
            || !StringUtils.hasText(request.getUserDetectTypeCode())) {
            throw new IllegalArgumentException("保存结果参数不完整");
        }

        String finalResult;
        String finalExtraInfo;
        if (request.getAnsweredQuestions() != null && !request.getAnsweredQuestions().isEmpty()) {
            ScoringOutput scoringOutput = scoreByConfiguredRules(request);
            finalResult = scoringOutput.detectResult;
            finalExtraInfo = scoringOutput.extraInfoJson;
        } else {
            // 兼容旧调用：若前端仍直接传结果，则继续支持。
            if (!StringUtils.hasText(request.getUserDetectResult())) {
                throw new IllegalArgumentException("缺少答题明细或检测结果");
            }
            finalResult = request.getUserDetectResult();
            finalExtraInfo = request.getExtraInfo();
        }

        LambdaQueryWrapper<UserDetectResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDetectResult::getUserId, request.getUserId())
            .eq(UserDetectResult::getUserDetectTypeCode, request.getUserDetectTypeCode());
        UserDetectResult exists = resultMapper.selectOne(wrapper);

        if (exists == null) {
            UserDetectResult current = new UserDetectResult();
            current.setUserId(request.getUserId());
            current.setUserDetectTypeCode(request.getUserDetectTypeCode());
            current.setUserDetectResult(finalResult);
            current.setExtraInfo(finalExtraInfo);
            resultMapper.insert(current);
        } else {
            exists.setUserDetectResult(finalResult);
            exists.setExtraInfo(finalExtraInfo);
            resultMapper.updateById(exists);
        }

        UserDetectResultHistory history = new UserDetectResultHistory();
        history.setUserId(request.getUserId());
        history.setUserDetectTypeCode(request.getUserDetectTypeCode());
        history.setUserDetectResult(finalResult);
        history.setExtraInfo(finalExtraInfo);
        historyMapper.insert(history);

        DetectionResultSaveResponse response = new DetectionResultSaveResponse();
        response.setDetectResult(finalResult);
        response.setExtraInfo(finalExtraInfo);
        return response;
    }

    @Override
    public UserDetectResult getCurrentResult(String userId, String detectTypeCode) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(detectTypeCode)) {
            return null;
        }
        LambdaQueryWrapper<UserDetectResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDetectResult::getUserId, userId)
            .eq(UserDetectResult::getUserDetectTypeCode, detectTypeCode);
        return resultMapper.selectOne(wrapper);
    }

    @Override
    public List<UserDetectResultHistory> listHistory(String userId, String detectTypeCode) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(detectTypeCode)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<UserDetectResultHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDetectResultHistory::getUserId, userId)
            .eq(UserDetectResultHistory::getUserDetectTypeCode, detectTypeCode)
            .orderByDesc(UserDetectResultHistory::getCreateTime)
            .orderByDesc(UserDetectResultHistory::getId);
        return historyMapper.selectList(wrapper);
    }

    private ScoringOutput scoreByConfiguredRules(DetectionResultSaveRequest request) {
        UserDetectionType detectionType = getDetectionType(request.getUserDetectTypeCode());
        List<UserDetectionQuestion> questionList = getQuestionsByTypeCode(request.getUserDetectTypeCode());
        if (questionList.isEmpty()) {
            throw new IllegalArgumentException("检测题目不存在");
        }

        Map<Long, UserDetectionQuestion> questionById = questionList.stream()
            .collect(Collectors.toMap(UserDetectionQuestion::getId, q -> q, (a, b) -> a));
        Map<String, UserDetectionQuestion> questionByCode = questionList.stream()
            .filter(q -> StringUtils.hasText(q.getQuestionCode()))
            .collect(Collectors.toMap(UserDetectionQuestion::getQuestionCode, q -> q, (a, b) -> a));

        Map<String, Integer> scoreByDimension = new HashMap<>();
        JSONArray answeredQuestions = new JSONArray();
        for (DetectionResultSaveRequest.AnsweredQuestion answer : request.getAnsweredQuestions()) {
            UserDetectionQuestion question = locateQuestion(answer, questionById, questionByCode);
            JSONObject selectedOption = locateSelectedOption(question, answer.getSelectedOptionKey());
            accumulateScores(selectedOption, scoreByDimension);
            answeredQuestions.add(buildAnsweredQuestionDetail(question, selectedOption, answer.getSelectedOptionKey()));
        }

        JSONObject scoringConfig = parseScoringConfig(detectionType);
        String scoringMode = resolveResultMode(detectionType, scoringConfig);
        DetectionScoringStrategy strategy = scoringStrategies.get(scoringMode);
        if (strategy == null) {
            throw new IllegalArgumentException("暂不支持的评分模式：" + scoringMode);
        }
        String detectResult = strategy.calculate(detectionType, scoringConfig, scoreByDimension);
        JSONObject extra = new JSONObject();
        extra.put("scoringMode", scoringMode);
        extra.put("scoringVersion", resolveScoringVersion(scoringConfig));
        extra.put("scoreByDimension", new JSONObject(new LinkedHashMap<>(scoreByDimension)));
        extra.put("answeredQuestions", answeredQuestions);

        ScoringOutput output = new ScoringOutput();
        output.detectResult = detectResult;
        output.extraInfoJson = JSON.toJSONString(extra);
        return output;
    }

    private UserDetectionType getDetectionType(String typeCode) {
        LambdaQueryWrapper<UserDetectionType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDetectionType::getTypeCode, typeCode).last("limit 1");
        UserDetectionType detectionType = typeMapper.selectOne(wrapper);
        if (detectionType == null) {
            throw new IllegalArgumentException("检测类型不存在");
        }
        return detectionType;
    }

    private List<UserDetectionQuestion> getQuestionsByTypeCode(String typeCode) {
        LambdaQueryWrapper<UserDetectionQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDetectionQuestion::getTypeCode, typeCode).orderByAsc(UserDetectionQuestion::getQuestionOrder);
        return questionMapper.selectList(wrapper);
    }

    private UserDetectionQuestion locateQuestion(
        DetectionResultSaveRequest.AnsweredQuestion answer,
        Map<Long, UserDetectionQuestion> questionById,
        Map<String, UserDetectionQuestion> questionByCode
    ) {
        UserDetectionQuestion question = null;
        if (answer != null && answer.getQuestionId() != null) {
            question = questionById.get(answer.getQuestionId());
        }
        if (question == null && answer != null && StringUtils.hasText(answer.getQuestionCode())) {
            question = questionByCode.get(answer.getQuestionCode());
        }
        if (question == null) {
            throw new IllegalArgumentException("题目不存在或不属于当前检测类型");
        }
        return question;
    }

    private JSONObject locateSelectedOption(UserDetectionQuestion question, String selectedOptionKey) {
        if (!StringUtils.hasText(selectedOptionKey)) {
            throw new IllegalArgumentException("存在未选择选项的题目");
        }
        JSONArray options = parseOptions(question.getOptions());
        for (int i = 0; i < options.size(); i++) {
            JSONObject optionObj = options.getJSONObject(i);
            if (optionObj == null) {
                continue;
            }
            String key = optionObj.getString("key");
            if (selectedOptionKey.equalsIgnoreCase(key)) {
                return optionObj;
            }
        }
        throw new IllegalArgumentException("题目选项无效：" + question.getQuestionCode());
    }

    private JSONArray parseOptions(String optionsJson) {
        try {
            JSONArray result = JSON.parseArray(StringUtils.hasText(optionsJson) ? optionsJson : "[]");
            return result == null ? new JSONArray() : result;
        } catch (Exception e) {
            throw new IllegalArgumentException("题目选项配置异常");
        }
    }

    private void accumulateScores(JSONObject selectedOption, Map<String, Integer> scoreByDimension) {
        JSONObject scores = selectedOption.getJSONObject("scores");
        if (scores != null) {
            for (Map.Entry<String, Object> entry : scores.entrySet()) {
                String dimension = entry.getKey();
                int value = Integer.parseInt(String.valueOf(entry.getValue()));
                scoreByDimension.put(dimension, scoreByDimension.getOrDefault(dimension, 0) + value);
            }
            return;
        }
        // 兼容历史题库：旧版只配置了 dimension，不含 scores。
        String dimension = selectedOption.getString("dimension");
        if (StringUtils.hasText(dimension)) {
            scoreByDimension.put(dimension, scoreByDimension.getOrDefault(dimension, 0) + 1);
        }
    }

    private JSONObject buildAnsweredQuestionDetail(UserDetectionQuestion question, JSONObject selectedOption, String selectedOptionKey) {
        JSONObject detail = new JSONObject();
        detail.put("questionId", question.getId());
        detail.put("questionCode", question.getQuestionCode());
        detail.put("questionTitle", question.getQuestionName());
        detail.put("selectedOptionKey", selectedOptionKey);
        detail.put("selectedOptionText", selectedOption.getString("text"));
        String dimension = selectedOption.getString("dimension");
        if (StringUtils.hasText(dimension)) {
            detail.put("selectedOptionDimension", dimension);
        }
        JSONObject scores = selectedOption.getJSONObject("scores");
        if (scores != null) {
            detail.put("selectedOptionScores", scores);
        }
        return detail;
    }

    private String resolveResultMode(UserDetectionType type, JSONObject scoring) {
        if (scoring != null && StringUtils.hasText(scoring.getString("resultMode"))) {
            return scoring.getString("resultMode");
        }
        // MBTI 缺省按 pair_compare 处理。
        if ("MBTI".equalsIgnoreCase(type.getTypeCode())) {
            return "pair_compare";
        }
        return "max_score";
    }

    private String resolveScoringVersion(JSONObject scoring) {
        if (scoring != null && StringUtils.hasText(scoring.getString("scoringVersion"))) {
            return scoring.getString("scoringVersion");
        }
        return "1.0";
    }

    private JSONObject parseScoringConfig(UserDetectionType type) {
        if (!StringUtils.hasText(type.getExtraInfo())) {
            return null;
        }
        try {
            JSONObject rootObj = JSON.parseObject(type.getExtraInfo());
            if (rootObj == null) {
                return null;
            }
            JSONObject scoring = rootObj.getJSONObject("scoring");
            if (scoring != null) {
                return scoring;
            }
            return rootObj;
        } catch (Exception e) {
            return null;
        }
    }

    private static class ScoringOutput {
        private String detectResult;
        private String extraInfoJson;
    }
}
