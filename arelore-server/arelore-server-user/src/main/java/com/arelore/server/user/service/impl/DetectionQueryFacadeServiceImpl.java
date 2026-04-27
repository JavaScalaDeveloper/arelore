package com.arelore.server.user.service.impl;

import com.arelore.server.core.detection.dto.DetectionPaperListRequest;
import com.arelore.server.core.detection.dto.DetectionResultQueryRequest;
import com.arelore.server.core.detection.dto.DetectionResultSaveRequest;
import com.arelore.server.core.detection.dto.DetectionResultSaveResponse;
import com.arelore.server.core.detection.dto.UserDetectionQuestionRequest;
import com.arelore.server.core.detection.dto.UserDetectionQuestionResponse;
import com.arelore.server.core.detection.dto.UserDetectionTypeRequest;
import com.arelore.server.core.detection.dto.UserDetectionTypeResponse;
import com.arelore.server.core.detection.entity.UserDetectResult;
import com.arelore.server.core.detection.entity.UserDetectResultHistory;
import com.arelore.server.core.detection.service.UserDetectionQuestionService;
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
    private final UserDetectionResultService resultService;

    public DetectionQueryFacadeServiceImpl(
        UserDetectionTypeService typeService,
        UserDetectionQuestionService questionService,
        UserDetectionResultService resultService
    ) {
        this.typeService = typeService;
        this.questionService = questionService;
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
    public List<UserDetectResultHistory> historyResult(DetectionResultQueryRequest request) {
        String userId = request == null ? null : request.getUserId();
        String typeCode = request == null ? null : request.getTypeCode();
        return resultService.listHistory(userId, typeCode);
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

