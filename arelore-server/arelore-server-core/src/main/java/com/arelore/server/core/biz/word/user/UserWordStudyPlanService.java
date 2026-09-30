package com.arelore.server.core.biz.word.user;

import com.arelore.server.core.biz.word.user.dto.UserWordStudyPlanRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyPlanResponse;
import com.arelore.server.core.service.BaseService;

public interface UserWordStudyPlanService extends BaseService<UserWordStudyPlanRequest, UserWordStudyPlanResponse> {
    /**
     * 保存（upsert）学习计划，并切换为当前词书、写入当日新学/复习待办。
     */
    UserWordStudyPlanResponse confirm(UserWordStudyPlanRequest request);

    /**
     * 仅更新当日进度相关 ext_info，不触发计划重置校验。
     */
    void saveDailyProgress(Long planId, String extInfo);
}
