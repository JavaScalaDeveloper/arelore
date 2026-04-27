package com.arelore.server.user.service;

import com.arelore.server.core.detection.dto.DetectionPaperListRequest;
import com.arelore.server.core.detection.dto.DetectionResultQueryRequest;
import com.arelore.server.core.detection.dto.DetectionResultSaveRequest;
import com.arelore.server.core.detection.dto.DetectionResultSaveResponse;
import com.arelore.server.core.detection.dto.UserDetectionQuestionRequest;
import com.arelore.server.core.detection.dto.UserDetectionQuestionResponse;
import com.arelore.server.core.detection.dto.UserDetectionTypeResponse;
import com.arelore.server.core.detection.entity.UserDetectResult;
import com.arelore.server.core.detection.entity.UserDetectResultHistory;

import java.util.List;

public interface DetectionQueryFacadeService {
    List<UserDetectionTypeResponse> listAllTypes();

    List<UserDetectionQuestionResponse> listQuestions(UserDetectionQuestionRequest request);

    List<UserDetectionTypeResponse> listPapers(DetectionPaperListRequest request);

    DetectionResultSaveResponse saveResult(DetectionResultSaveRequest request);

    UserDetectResult currentResult(DetectionResultQueryRequest request);

    List<UserDetectResultHistory> historyResult(DetectionResultQueryRequest request);
}

