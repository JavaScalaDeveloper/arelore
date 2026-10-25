package com.arelore.server.core.biz.prompt.user;

import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptImportResult;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptRequest;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptResponse;
import com.arelore.server.core.service.BaseService;

import java.math.BigDecimal;

public interface UserAiPromptService extends BaseService<UserAiPromptRequest, UserAiPromptResponse> {
    UserAiPromptResponse saveOrUpsert(UserAiPromptRequest request);

    UserAiPromptImportResult importBatch(BigDecimal userId, UserAiPromptRequest request);

    int softDelete(BigDecimal userId, Long id);
}
