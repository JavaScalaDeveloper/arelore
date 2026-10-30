package com.arelore.server.core.biz.prompt.user.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserAiPromptPrefRequest extends UserAiPromptPrefResponse {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
