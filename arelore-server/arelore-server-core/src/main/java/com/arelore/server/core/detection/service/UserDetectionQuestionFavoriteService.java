package com.arelore.server.core.detection.service;

import com.arelore.server.core.common.service.BaseService;
import com.arelore.server.core.detection.dto.DetectionQuestionFavoriteToggleRequest;
import com.arelore.server.core.detection.dto.UserDetectionQuestionFavoriteRequest;
import com.arelore.server.core.detection.dto.UserDetectionQuestionFavoriteResponse;

import java.util.List;

public interface UserDetectionQuestionFavoriteService
    extends BaseService<UserDetectionQuestionFavoriteRequest, UserDetectionQuestionFavoriteResponse> {

    boolean toggleFavorite(DetectionQuestionFavoriteToggleRequest request);

    List<String> listFavoriteQuestionCodes(String userId, String questionTypeCode);

    List<UserDetectionQuestionFavoriteResponse> listFavorites(String userId);
}
