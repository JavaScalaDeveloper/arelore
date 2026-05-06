package com.arelore.server.user.service.impl;

import com.arelore.server.core.detection.dto.DetectionPaperListRequest;
import com.arelore.server.core.detection.dto.DetectionQuestionQueryRequest;
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
import com.arelore.server.core.detection.service.UserDetectionQuestionService;
import com.arelore.server.core.detection.service.UserDetectionQuestionFavoriteService;
import com.arelore.server.core.detection.service.UserDetectionResultService;
import com.arelore.server.core.detection.service.UserDetectionTypeService;
import com.arelore.server.user.service.DetectionQueryFacadeService;
import org.springframework.stereotype.Service;

import java.util.List;
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

