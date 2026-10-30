package com.arelore.server.core.biz.prompt.user;

import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptPrefRequest;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptPrefResponse;
import com.arelore.server.core.service.BaseService;

import java.math.BigDecimal;

public interface UserAiPromptPrefService extends BaseService<UserAiPromptPrefRequest, UserAiPromptPrefResponse> {
    /** 当前用户偏好；无记录时返回默认值（不落库） */
    UserAiPromptPrefResponse getOrDefault(BigDecimal userId);

    /** 按 userId upsert 偏好（ext_json 整包） */
    UserAiPromptPrefResponse savePref(UserAiPromptPrefRequest request);
}
