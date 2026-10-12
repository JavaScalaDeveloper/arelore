package com.arelore.server.core.biz.word.user;

import com.arelore.server.core.biz.word.user.dto.UserWordPreferenceRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordPreferenceResponse;
import com.arelore.server.core.service.BaseService;

import java.math.BigDecimal;

public interface UserWordPreferenceService extends BaseService<UserWordPreferenceRequest, UserWordPreferenceResponse> {

    /**
     * 获取当前用户偏好；无记录时返回默认值（不落库）。
     */
    UserWordPreferenceResponse getOrDefault(BigDecimal userId);

    /**
     * 保存/更新偏好（一人一行 upsert）。
     */
    UserWordPreferenceResponse savePreference(UserWordPreferenceRequest request);
}
