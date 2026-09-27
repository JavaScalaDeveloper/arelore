package com.arelore.server.core.service.cert.user;

import com.arelore.server.core.service.BaseService;
import com.arelore.server.core.detection.dto.DetectionResultSaveRequest;
import com.arelore.server.core.detection.dto.DetectionResultSaveResponse;
import com.arelore.server.core.detection.dto.UserDetectResultHistoryResponse;
import com.arelore.server.core.detection.dto.UserDetectResultRequest;
import com.arelore.server.core.detection.dto.UserDetectResultResponse;

import java.util.List;

public interface UserDetectionResultService extends BaseService<UserDetectResultRequest, UserDetectResultResponse> {
    DetectionResultSaveResponse saveResultAndHistory(DetectionResultSaveRequest request);

    UserDetectResultResponse getCurrentResult(String userId, String detectTypeCode);

    List<UserDetectResultHistoryResponse> listHistory(String userId, String detectTypeCode);

    List<UserDetectResultHistoryResponse> listSubmittedHistory(String userId);

    int redoResult(String userId, String detectTypeCode);
}
