package com.arelore.server.core.detection.service;

import com.arelore.server.core.common.service.BaseService;
import com.arelore.server.core.detection.dto.UserDetectionQuestionRequest;
import com.arelore.server.core.detection.dto.UserDetectionQuestionResponse;

import java.util.List;

public interface UserDetectionQuestionService extends BaseService<UserDetectionQuestionRequest, UserDetectionQuestionResponse> {
    List<UserDetectionQuestionResponse> listByTypeCode(String typeCode);

    /**
     * 按试卷类型与题目编码查询单题（用于收藏详情等只读场景）。
     */
    UserDetectionQuestionResponse getByTypeAndQuestionCode(String typeCode, String questionCode);
}
