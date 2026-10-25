package com.arelore.server.core.biz.prompt.user;

import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptTagRequest;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptTagResponse;
import com.arelore.server.core.service.BaseService;

import java.math.BigDecimal;
import java.util.Collection;

public interface UserAiPromptTagService extends BaseService<UserAiPromptTagRequest, UserAiPromptTagResponse> {
    UserAiPromptTagResponse saveTag(UserAiPromptTagRequest request);

    int softDelete(BigDecimal userId, Long id);

    /** 按类型确保标签名存在于字典（导入/页面保存时登记） */
    void ensureTags(BigDecimal userId, String tagType, Collection<String> names);

    default void ensureConfigTags(BigDecimal userId, Collection<String> names) {
        ensureTags(userId, "CONFIG", names);
    }
}
