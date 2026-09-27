package com.arelore.server.core.service.cert.user;

import com.arelore.server.core.service.BaseService;
import com.arelore.server.core.detection.dto.DetectionQuestionFavoriteToggleRequest;
import com.arelore.server.core.detection.dto.UserDetectionQuestionFavoriteRequest;
import com.arelore.server.core.detection.dto.UserDetectionQuestionFavoriteResponse;
import com.arelore.server.core.detection.entity.UserDetectionQuestionFavorite;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface UserDetectionQuestionFavoriteService
    extends BaseService<UserDetectionQuestionFavoriteRequest, UserDetectionQuestionFavoriteResponse> {

    boolean toggleFavorite(DetectionQuestionFavoriteToggleRequest request);

    List<String> listFavoriteQuestionCodes(String userId, String questionTypeCode);

    List<UserDetectionQuestionFavoriteResponse> listFavorites(String userId);

    /**
     * 按收藏时间倒序分页。
     */
    IPage<UserDetectionQuestionFavorite> pageFavorites(String userId, int pageNum, int pageSize);
}
