package com.arelore.server.core.detection.service;

import com.arelore.server.core.common.service.BaseService;
import com.arelore.server.core.detection.dto.DetectionResultSaveRequest;
import com.arelore.server.core.detection.dto.DetectionResultSaveResponse;
import com.arelore.server.core.detection.dto.UserDetectResultRequest;
import com.arelore.server.core.detection.dto.UserDetectResultResponse;
import com.arelore.server.core.detection.entity.UserDetectResultHistory;

import java.util.List;

public interface UserDetectionResultService extends BaseService<UserDetectResultRequest, UserDetectResultResponse> {
    DetectionResultSaveResponse saveResultAndHistory(DetectionResultSaveRequest request);

    UserDetectResultResponse getCurrentResult(String userId, String detectTypeCode);

    List<UserDetectResultHistory> listHistory(String userId, String detectTypeCode);
}
