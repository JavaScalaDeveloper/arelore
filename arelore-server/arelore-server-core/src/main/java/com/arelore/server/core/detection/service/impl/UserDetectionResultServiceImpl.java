package com.arelore.server.core.detection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.arelore.server.core.common.service.impl.BaseServiceImpl;
import com.arelore.server.core.detection.dto.DetectionResultSaveRequest;
import com.arelore.server.core.detection.dto.DetectionResultSaveResponse;
import com.arelore.server.core.detection.dto.UserDetectResultHistoryResponse;
import com.arelore.server.core.detection.dto.UserDetectResultRequest;
import com.arelore.server.core.detection.dto.UserDetectResultResponse;
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
import org.springframework.beans.BeanUtils;
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
public class UserDetectionResultServiceImpl
    extends BaseServiceImpl<UserDetectResultRequest, UserDetectResultResponse, UserDetectResult>
    implements UserDetectionResultService {
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
    protected UserDetectResultMapper mapper() {
        return resultMapper;
    }

    @Override
    protected Class<UserDetectResultResponse> responseClass() {
        return UserDetectResultResponse.class;
    }

    @Override
    protected LambdaQueryWrapper<UserDetectResult> buildWrapper(UserDetectResultRequest request) {
        LambdaQueryWrapper<UserDetectResult> w = new LambdaQueryWrapper<>();
        if (request == null) {
            return w;
        }
        if (StringUtils.hasText(request.getUserId())) {
            w.eq(UserDetectResult::getUserId, request.getUserId());
        }
        if (StringUtils.hasText(request.getUserDetectTypeCode())) {
            w.eq(UserDetectResult::getUserDetectTypeCode, request.getUserDetectTypeCode());
        }
        if (StringUtils.hasText(request.getUserDetectResult())) {
            w.eq(UserDetectResult::getUserDetectResult, request.getUserDetectResult());
        }
        w.orderByDesc(UserDetectResult::getModifyTime).orderByDesc(UserDetectResult::getId);
        return w;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DetectionResultSaveResponse saveResultAndHistory(DetectionResultSaveRequest request) {
        if (request == null
            || !StringUtils.hasText(request.getUserId())
            || !StringUtils.hasText(request.getUserDetectTypeCode())) {
            throw new IllegalArgumentException("保存结果参数不完整");
        }

        LambdaQueryWrapper<UserDetectResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDetectResult::getUserId, request.getUserId())
            .eq(UserDetectResult::getUserDetectTypeCode, request.getUserDetectTypeCode());
        UserDetectResult exists = resultMapper.selectOne(wrapper);

        boolean hasAnswers = request.getAnsweredQuestions() != null && !request.getAnsweredQuestions().isEmpty();
        boolean submitPaper = Boolean.TRUE.equals(request.getSubmitPaper());
        String finalResult;
        String finalExtraInfo;
        if (submitPaper) {
            UserDetectionType detectionTypeEarly = getDetectionType(request.getUserDetectTypeCode());
            JSONObject scoringConfigEarly = parseScoringConfig(detectionTypeEarly);
            String scoringModeEarly = resolveResultMode(detectionTypeEarly, scoringConfigEarly);
            if ("objective_sum".equals(scoringModeEarly)) {
                // 客观卷：按题库全部题目计分，允许白卷或部分作答
                ScoringOutput scoringOutput = scoreFullPaperObjectiveSubmit(request);
                finalResult = scoringOutput.detectResult;
                finalExtraInfo = scoringOutput.extraInfoJson;
            } else if (hasAnswers) {
                ScoringOutput scoringOutput = scoreByConfiguredRules(request);
                finalResult = scoringOutput.detectResult;
                finalExtraInfo = scoringOutput.extraInfoJson;
            } else if (StringUtils.hasText(request.getUserDetectResult())) {
                finalResult = request.getUserDetectResult();
                finalExtraInfo = request.getExtraInfo();
            } else {
                throw new IllegalArgumentException("缺少答题明细或检测结果");
            }
        } else if (hasAnswers) {
            // 答题中保存：仅刷新 extra_info，不提前写入最终结果
            finalResult = exists == null ? "" : (exists.getUserDetectResult() == null ? "" : exists.getUserDetectResult());
            finalExtraInfo = buildProgressExtraInfo(request);
        } else {
            // 允许在没有 answeredQuestions 时保存进度（例如仅切题）
            finalResult = exists == null ? "" : (exists.getUserDetectResult() == null ? "" : exists.getUserDetectResult());
            finalExtraInfo = StringUtils.hasText(request.getExtraInfo()) ? request.getExtraInfo() : "{}";
        }

        if (exists == null) {
            UserDetectResult current = new UserDetectResult();
            current.setUserId(request.getUserId());
            current.setUserDetectTypeCode(request.getUserDetectTypeCode());
            current.setUserDetectResult(finalResult);
            current.setExtraInfo(finalExtraInfo);
            resultMapper.insert(current);
        } else {
            if (submitPaper) {
                exists.setUserDetectResult(finalResult);
            }
            exists.setExtraInfo(finalExtraInfo);
            resultMapper.updateById(exists);
        }

        // 答题中保存进度不写 history：避免每条进度产生大 JSON 插入，显著降低响应时间与库压力；交卷时再记录。
        if (submitPaper) {
            UserDetectResultHistory history = new UserDetectResultHistory();
            history.setUserId(request.getUserId());
            history.setUserDetectTypeCode(request.getUserDetectTypeCode());
            history.setUserDetectResult(finalResult);
            history.setExtraInfo(finalExtraInfo);
            historyMapper.insert(history);
        }

        DetectionResultSaveResponse response = new DetectionResultSaveResponse();
        response.setDetectResult(finalResult);
        response.setExtraInfo(finalExtraInfo);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int redoResult(String userId, String detectTypeCode) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(detectTypeCode)) {
            return 0;
        }
        LambdaQueryWrapper<UserDetectResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDetectResult::getUserId, userId)
            .eq(UserDetectResult::getUserDetectTypeCode, detectTypeCode);
        // 仅删除 current，不删除 history
        return resultMapper.delete(wrapper);
    }

    @Override
    public UserDetectResultResponse getCurrentResult(String userId, String detectTypeCode) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(detectTypeCode)) {
            return null;
        }
        LambdaQueryWrapper<UserDetectResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDetectResult::getUserId, userId)
            .eq(UserDetectResult::getUserDetectTypeCode, detectTypeCode);
        return toResponse(resultMapper.selectOne(wrapper));
    }

    @Override
    public List<UserDetectResultHistoryResponse> listHistory(String userId, String detectTypeCode) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(detectTypeCode)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<UserDetectResultHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDetectResultHistory::getUserId, userId)
            .eq(UserDetectResultHistory::getUserDetectTypeCode, detectTypeCode)
            .orderByDesc(UserDetectResultHistory::getCreateTime)
            .orderByDesc(UserDetectResultHistory::getId);
        return toHistoryResponses(historyMapper.selectList(wrapper));
    }

    @Override
    public List<UserDetectResultHistoryResponse> listSubmittedHistory(String userId) {
        if (!StringUtils.hasText(userId)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<UserDetectResultHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDetectResultHistory::getUserId, userId)
            .isNotNull(UserDetectResultHistory::getUserDetectResult)
            .ne(UserDetectResultHistory::getUserDetectResult, "")
            .orderByDesc(UserDetectResultHistory::getCreateTime)
            .orderByDesc(UserDetectResultHistory::getId);
        return toHistoryResponses(historyMapper.selectList(wrapper));
    }

    private List<UserDetectResultHistoryResponse> toHistoryResponses(List<UserDetectResultHistory> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(it -> {
            if (it == null) {
                return null;
            }
            UserDetectResultHistoryResponse resp = new UserDetectResultHistoryResponse();
            BeanUtils.copyProperties(it, resp);
            return resp;
        }).filter(e -> e != null).collect(Collectors.toList());
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

    private JSONObject buildUnansweredQuestionDetail(UserDetectionQuestion question) {
        JSONObject detail = new JSONObject();
        detail.put("questionId", question.getId());
        detail.put("questionCode", question.getQuestionCode());
        detail.put("questionTitle", question.getQuestionName());
        detail.put("selectedOptionKey", "");
        detail.put("selectedOptionText", "(未作答)");
        detail.put("unanswered", true);
        return detail;
    }

    /**
     * 客观卷整卷交卷：遍历该类型下全部题目，未作答不得分；允许白卷。
     */
    private ScoringOutput scoreFullPaperObjectiveSubmit(DetectionResultSaveRequest request) {
        UserDetectionType detectionType = getDetectionType(request.getUserDetectTypeCode());
        List<UserDetectionQuestion> questionList = getQuestionsByTypeCode(request.getUserDetectTypeCode());
        if (questionList.isEmpty()) {
            throw new IllegalArgumentException("检测题目不存在");
        }

        Map<String, String> answerByCode = new HashMap<>();
        Map<Long, String> answerById = new HashMap<>();
        if (request.getAnsweredQuestions() != null) {
            for (DetectionResultSaveRequest.AnsweredQuestion a : request.getAnsweredQuestions()) {
                if (a == null || !StringUtils.hasText(a.getSelectedOptionKey())) {
                    continue;
                }
                if (a.getQuestionId() != null) {
                    answerById.put(a.getQuestionId(), a.getSelectedOptionKey());
                }
                if (StringUtils.hasText(a.getQuestionCode())) {
                    answerByCode.put(a.getQuestionCode(), a.getSelectedOptionKey());
                }
            }
        }

        Map<String, Integer> scoreByDimension = new HashMap<>();
        JSONArray answeredQuestions = new JSONArray();
        int answeredCount = 0;
        for (UserDetectionQuestion question : questionList) {
            String selectedKey = null;
            if (question.getId() != null) {
                selectedKey = answerById.get(question.getId());
            }
            if (!StringUtils.hasText(selectedKey) && StringUtils.hasText(question.getQuestionCode())) {
                selectedKey = answerByCode.get(question.getQuestionCode());
            }
            if (!StringUtils.hasText(selectedKey)) {
                answeredQuestions.add(buildUnansweredQuestionDetail(question));
                continue;
            }
            answeredCount++;
            try {
                JSONObject selectedOption = locateSelectedOption(question, selectedKey);
                accumulateScores(selectedOption, scoreByDimension);
                answeredQuestions.add(buildAnsweredQuestionDetail(question, selectedOption, selectedKey));
            } catch (IllegalArgumentException ex) {
                JSONObject detail = new JSONObject();
                detail.put("questionId", question.getId());
                detail.put("questionCode", question.getQuestionCode());
                detail.put("questionTitle", question.getQuestionName());
                detail.put("selectedOptionKey", selectedKey);
                detail.put("selectedOptionText", "(选项无效)");
                answeredQuestions.add(detail);
            }
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
        extra.put("submitPaper", true);
        extra.put("totalQuestions", questionList.size());
        extra.put("answeredCount", answeredCount);
        extra.put("unansweredCount", questionList.size() - answeredCount);

        ScoringOutput output = new ScoringOutput();
        output.detectResult = detectResult;
        output.extraInfoJson = JSON.toJSONString(extra);
        return output;
    }

    /**
     * 答题中进度保存：仅写入答题清单（题目 id / code / 所选 key），不展开题干与选项文案。
     * 前端恢复进度只依赖 questionCode + selectedOptionKey；交卷时仍走 {@link #scoreByConfiguredRules} 完整计分。
     */
    private String buildProgressExtraInfo(DetectionResultSaveRequest request) {
        JSONArray answeredQuestions = new JSONArray();
        for (DetectionResultSaveRequest.AnsweredQuestion answer : request.getAnsweredQuestions()) {
            if (answer == null) {
                continue;
            }
            JSONObject row = new JSONObject();
            if (answer.getQuestionId() != null) {
                row.put("questionId", answer.getQuestionId());
            }
            if (StringUtils.hasText(answer.getQuestionCode())) {
                row.put("questionCode", answer.getQuestionCode());
            }
            if (StringUtils.hasText(answer.getSelectedOptionKey())) {
                row.put("selectedOptionKey", answer.getSelectedOptionKey());
            }
            answeredQuestions.add(row);
        }

        JSONObject extra = new JSONObject();
        extra.put("submitPaper", false);
        extra.put("answeredQuestions", answeredQuestions);
        if (StringUtils.hasText(request.getExtraInfo())) {
            try {
                extra.put("progress", JSON.parseObject(request.getExtraInfo()));
            } catch (Exception e) {
                extra.put("progressRaw", request.getExtraInfo());
            }
        }
        return JSON.toJSONString(extra);
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
