package com.arelore.server.core.biz.prompt.user;

import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptDailyVisitRequest;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptDailyVisitResponse;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptVisitStatsResponse;
import com.arelore.server.core.service.BaseService;

import java.math.BigDecimal;
import java.util.List;

public interface UserAiPromptDailyVisitService
    extends BaseService<UserAiPromptDailyVisitRequest, UserAiPromptDailyVisitResponse> {

    /** 上报一次访问（失败不抛） */
    void recordVisit(BigDecimal userId, String client);

    /** 按日汇总 PV/UV */
    List<UserAiPromptVisitStatsResponse> statsByDay(UserAiPromptDailyVisitRequest request);
}
