package com.arelore.server.user.service.impl;

import com.arelore.server.core.detection.dto.DetectionPaperListRequest;
import com.arelore.server.core.detection.dto.DetectionQuestionFavoriteItemResponse;
import com.arelore.server.core.detection.dto.DetectionQuestionFavoritePageRequest;
import com.arelore.server.core.detection.dto.DetectionQuestionFavoritePageResponse;
import com.arelore.server.core.detection.dto.DetectionQuestionQueryRequest;
import com.arelore.server.core.detection.dto.QuestionTypeCodePair;
import com.arelore.server.core.detection.dto.DetectionQuestionFavoriteListRequest;
import com.arelore.server.core.detection.dto.DetectionQuestionFavoriteToggleRequest;
import com.arelore.server.core.detection.dto.DetectionResultQueryRequest;
import com.arelore.server.core.detection.dto.DetectionResultSaveRequest;
import com.arelore.server.core.detection.dto.DetectionResultSaveResponse;
import com.arelore.server.core.detection.dto.UserDetectResultHistoryResponse;
import com.arelore.server.core.detection.dto.UserDetectionQuestionFavoriteResponse;
import com.arelore.server.core.detection.dto.UserDetectionQuestionRequest;
import com.arelore.server.core.detection.dto.UserDetectionQuestionResponse;
import com.arelore.server.core.detection.dto.UserDetectionTypeRequest;
import com.arelore.server.core.detection.dto.UserDetectionTypeResponse;
import com.arelore.server.core.detection.entity.UserDetectResult;
import com.arelore.server.core.detection.entity.UserDetectionQuestionFavorite;
import com.arelore.server.core.detection.service.UserDetectionQuestionService;
import com.arelore.server.core.detection.service.UserDetectionQuestionFavoriteService;
import com.arelore.server.core.detection.service.UserDetectionResultService;
import com.arelore.server.core.detection.service.UserDetectionTypeService;
import com.arelore.server.user.service.DetectionQueryFacadeService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DetectionQueryFacadeServiceImpl implements DetectionQueryFacadeService {
    private final UserDetectionTypeService typeService;
    private final UserDetectionQuestionService questionService;
    private final UserDetectionQuestionFavoriteService questionFavoriteService;
    private final UserDetectionResultService resultService;

    public DetectionQueryFacadeServiceImpl(
        UserDetectionTypeService typeService,
        UserDetectionQuestionService questionService,
        UserDetectionQuestionFavoriteService questionFavoriteService,
        UserDetectionResultService resultService
    ) {
        this.typeService = typeService;
        this.questionService = questionService;
        this.questionFavoriteService = questionFavoriteService;
        this.resultService = resultService;
    }

    @Override
    public List<UserDetectionTypeResponse> listAllTypes() {
        return typeService.listAll();
    }

    @Override
    public List<UserDetectionQuestionResponse> listQuestions(UserDetectionQuestionRequest request) {
        String typeCode = request == null ? null : request.getTypeCode();
        return questionService.listByTypeCode(typeCode);
    }

    @Override
    public List<UserDetectionTypeResponse> listPapers(DetectionPaperListRequest request) {
        String examCategory = request == null ? null : request.getExamCategory();
        String subject = request == null ? null : request.getSubject();
        List<UserDetectionTypeResponse> all = typeService.listAll();
        return all.stream()
            .filter(t -> matchKeyword(t, subject))
            .filter(t -> matchKeyword(t, examCategory))
            .collect(Collectors.toList());
    }

    @Override
    public DetectionResultSaveResponse saveResult(DetectionResultSaveRequest request) {
        return resultService.saveResultAndHistory(request);
    }

    @Override
    public UserDetectResult currentResult(DetectionResultQueryRequest request) {
        String userId = request == null ? null : request.getUserId();
        String typeCode = request == null ? null : request.getTypeCode();
        return resultService.getCurrentResult(userId, typeCode);
    }

    @Override
    public List<UserDetectResultHistoryResponse> historyResult(DetectionResultQueryRequest request) {
        String userId = request == null ? null : request.getUserId();
        String typeCode = request == null ? null : request.getTypeCode();
        return resultService.listHistory(userId, typeCode);
    }

    @Override
    public int redoResult(DetectionResultQueryRequest request) {
        String userId = request == null ? null : request.getUserId();
        String typeCode = request == null ? null : request.getTypeCode();
        return resultService.redoResult(userId, typeCode);
    }

    @Override
    public boolean toggleQuestionFavorite(DetectionQuestionFavoriteToggleRequest request) {
        return questionFavoriteService.toggleFavorite(request);
    }

    @Override
    public List<String> listQuestionFavoriteCodes(DetectionQuestionFavoriteListRequest request) {
        String userId = request == null ? null : request.getUserId();
        String typeCode = request == null ? null : request.getQuestionTypeCode();
        return questionFavoriteService.listFavoriteQuestionCodes(userId, typeCode);
    }

    @Override
    public List<UserDetectResultHistoryResponse> listSubmittedHistory(DetectionResultQueryRequest request) {
        String userId = request == null ? null : request.getUserId();
        return resultService.listSubmittedHistory(userId);
    }

    @Override
    public List<UserDetectionQuestionFavoriteResponse> listFavorites(DetectionResultQueryRequest request) {
        String userId = request == null ? null : request.getUserId();
        return questionFavoriteService.listFavorites(userId);
    }

    @Override
    public DetectionQuestionFavoritePageResponse listFavoritesPage(DetectionQuestionFavoritePageRequest request) {
        String userId = request == null ? null : request.getUserId();
        int pageNum = request == null || request.getPageNum() == null || request.getPageNum() < 1
            ? 1
            : request.getPageNum();
        int pageSize = request == null || request.getPageSize() == null || request.getPageSize() < 1
            ? 20
            : Math.min(request.getPageSize(), 100);

        DetectionQuestionFavoritePageResponse out = new DetectionQuestionFavoritePageResponse();
        out.setPageNum(pageNum);
        out.setPageSize(pageSize);
        if (!StringUtils.hasText(userId)) {
            out.setTotal(0);
            out.setRecords(Collections.emptyList());
            return out;
        }

        IPage<UserDetectionQuestionFavorite> page = questionFavoriteService.pageFavorites(userId, pageNum, pageSize);
        out.setTotal(page.getTotal());
        List<UserDetectionQuestionFavorite> rows = page.getRecords();
        if (rows.isEmpty()) {
            out.setRecords(Collections.emptyList());
            return out;
        }

        List<String> typeCodes = rows.stream()
            .map(UserDetectionQuestionFavorite::getQuestionTypeCode)
            .filter(StringUtils::hasText)
            .distinct()
            .collect(Collectors.toList());
        Map<String, String> typeNameByCode = typeService.listByTypeCodes(typeCodes).stream()
            .filter(Objects::nonNull)
            .filter(t -> StringUtils.hasText(t.getTypeCode()))
            .collect(Collectors.toMap(
                UserDetectionTypeResponse::getTypeCode,
                t -> StringUtils.hasText(t.getTypeName()) ? t.getTypeName() : "",
                (a, b) -> a
            ));

        List<QuestionTypeCodePair> pairs = rows.stream()
            .filter(r -> StringUtils.hasText(r.getQuestionTypeCode()) && StringUtils.hasText(r.getQuestionCode()))
            .map(r -> new QuestionTypeCodePair(r.getQuestionTypeCode(), r.getQuestionCode()))
            .collect(Collectors.toList());
        Map<String, UserDetectionQuestionResponse> qByKey = questionService.listByTypeAndQuestionCodePairs(pairs).stream()
            .filter(Objects::nonNull)
            .filter(q -> StringUtils.hasText(q.getTypeCode()) && StringUtils.hasText(q.getQuestionCode()))
            .collect(Collectors.toMap(
                q -> q.getTypeCode() + "\0" + q.getQuestionCode(),
                q -> q,
                (a, b) -> a
            ));

        List<DetectionQuestionFavoriteItemResponse> records = new ArrayList<>();
        for (UserDetectionQuestionFavorite row : rows) {
            DetectionQuestionFavoriteItemResponse it = new DetectionQuestionFavoriteItemResponse();
            it.setId(row.getId());
            it.setCreateTime(row.getCreateTime());
            it.setQuestionTypeCode(row.getQuestionTypeCode());
            it.setQuestionCode(row.getQuestionCode());
            String tc = row.getQuestionTypeCode();
            it.setTypeName(typeNameByCode.getOrDefault(tc, ""));
            String key = tc + "\0" + row.getQuestionCode();
            UserDetectionQuestionResponse q = qByKey.get(key);
            it.setQuestionName(q != null ? q.getQuestionName() : null);
            records.add(it);
        }
        out.setRecords(records);
        return out;
    }

    @Override
    public UserDetectionQuestionResponse getQuestionOne(DetectionQuestionQueryRequest request) {
        String typeCode = request == null ? null : request.getTypeCode();
        String questionCode = request == null ? null : request.getQuestionCode();
        return questionService.getByTypeAndQuestionCode(typeCode, questionCode);
    }

    private boolean matchKeyword(UserDetectionTypeResponse type, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        String k = keyword.trim();
        String name = type.getTypeName() == null ? "" : type.getTypeName();
        String desc = type.getTypeDescription() == null ? "" : type.getTypeDescription();
        String ext = type.getExtraInfo() == null ? "" : type.getExtraInfo();
        return name.contains(k) || desc.contains(k) || ext.contains(k);
    }
}

