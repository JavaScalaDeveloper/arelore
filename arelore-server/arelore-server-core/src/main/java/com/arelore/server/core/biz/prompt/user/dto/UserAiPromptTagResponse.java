package com.arelore.server.core.biz.prompt.user.dto;

import com.arelore.server.core.biz.prompt.user.entity.UserAiPromptTag;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserAiPromptTagResponse extends UserAiPromptTag {
    /** userId 字符串 */
    private String idStr;
}
