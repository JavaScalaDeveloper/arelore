package com.arelore.server.core.biz.common;

import com.arelore.server.core.biz.common.dto.AdminCommonUserDailyVisitRequest;
import com.arelore.server.core.biz.common.dto.AdminCommonUserDailyVisitResponse;
import com.arelore.server.core.service.BaseService;

import java.util.List;

public interface AdminCommonUserDailyVisitService
    extends BaseService<AdminCommonUserDailyVisitRequest, AdminCommonUserDailyVisitResponse> {

    int deleteByIds(List<Long> ids);
}
