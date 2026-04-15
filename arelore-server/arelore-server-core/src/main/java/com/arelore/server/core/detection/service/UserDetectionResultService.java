package com.arelore.server.core.detection.service;

import com.arelore.server.core.detection.dto.DetectionResultSaveRequest;
import com.arelore.server.core.detection.dto.DetectionResultSaveResponse;
import com.arelore.server.core.detection.entity.UserDetectResult;
import com.arelore.server.core.detection.entity.UserDetectResultHistory;

import java.util.List;

public interface UserDetectionResultService {
    DetectionResultSaveResponse saveResultAndHistory(DetectionResultSaveRequest request);

    UserDetectResult getCurrentResult(String userId, String detectTypeCode);

    List<UserDetectResultHistory> listHistory(String userId, String detectTypeCode);
}
