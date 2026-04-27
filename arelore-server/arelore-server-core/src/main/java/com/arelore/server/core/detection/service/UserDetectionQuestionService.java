package com.arelore.server.core.detection.service;

import com.arelore.server.core.common.service.BaseService;
import com.arelore.server.core.detection.dto.UserDetectionQuestionRequest;
import com.arelore.server.core.detection.dto.UserDetectionQuestionResponse;

import java.util.List;

public interface UserDetectionQuestionService extends BaseService<UserDetectionQuestionRequest, UserDetectionQuestionResponse> {
    List<UserDetectionQuestionResponse> listByTypeCode(String typeCode);
}
