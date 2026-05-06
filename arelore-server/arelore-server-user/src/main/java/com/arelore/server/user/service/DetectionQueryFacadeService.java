package com.arelore.server.user.service;

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
import com.arelore.server.core.detection.dto.UserDetectionTypeResponse;
import com.arelore.server.core.detection.entity.UserDetectResult;

import java.util.List;

public interface DetectionQueryFacadeService {
    List<UserDetectionTypeResponse> listAllTypes();

    List<UserDetectionQuestionResponse> listQuestions(UserDetectionQuestionRequest request);

    List<UserDetectionTypeResponse> listPapers(DetectionPaperListRequest request);

    DetectionResultSaveResponse saveResult(DetectionResultSaveRequest request);

    UserDetectResult currentResult(DetectionResultQueryRequest request);

    List<UserDetectResultHistoryResponse> historyResult(DetectionResultQueryRequest request);

    int redoResult(DetectionResultQueryRequest request);

    boolean toggleQuestionFavorite(DetectionQuestionFavoriteToggleRequest request);

    List<String> listQuestionFavoriteCodes(DetectionQuestionFavoriteListRequest request);

    List<UserDetectResultHistoryResponse> listSubmittedHistory(DetectionResultQueryRequest request);

    List<UserDetectionQuestionFavoriteResponse> listFavorites(DetectionResultQueryRequest request);

    /**
     * 按 typeCode + questionCode 查询单题（只读，用于收藏详情等）。
     */
    UserDetectionQuestionResponse getQuestionOne(DetectionQuestionQueryRequest request);
}

