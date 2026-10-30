package com.arelore.server.core.biz.prompt.user.dto;

import com.arelore.server.core.biz.prompt.user.entity.UserAiPromptDailyVisit;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserAiPromptDailyVisitResponse extends UserAiPromptDailyVisit {
    /** 用户业务 ID 字符串，对应 userId */
    private String idStr;
}
