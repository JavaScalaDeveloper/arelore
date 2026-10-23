package com.arelore.server.core.biz.common;

import com.arelore.server.core.biz.common.dto.AdminCommonUserDailyVisitRequest;
import com.arelore.server.core.biz.common.dto.AdminCommonUserDailyVisitResponse;
import com.arelore.server.core.biz.word.user.support.WordUserIdStrSupport;
import com.arelore.server.core.registration.entity.CommonUserDailyVisit;
import com.arelore.server.core.registration.mapper.CommonUserDailyVisitMapper;
import com.arelore.server.core.service.BaseServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminCommonUserDailyVisitServiceImpl
    extends BaseServiceImpl<AdminCommonUserDailyVisitRequest, AdminCommonUserDailyVisitResponse, CommonUserDailyVisit>
    implements AdminCommonUserDailyVisitService {

    private final CommonUserDailyVisitMapper mapper;

    public AdminCommonUserDailyVisitServiceImpl(CommonUserDailyVisitMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected CommonUserDailyVisitMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<AdminCommonUserDailyVisitResponse> responseClass() {
        return AdminCommonUserDailyVisitResponse.class;
    }

    @Override
    protected LambdaQueryWrapper<CommonUserDailyVisit> buildWrapper(AdminCommonUserDailyVisitRequest request) {
        LambdaQueryWrapper<CommonUserDailyVisit> wrapper = new LambdaQueryWrapper<>();
        if (request == null) {
            wrapper.orderByDesc(CommonUserDailyVisit::getVisitDate)
                .orderByDesc(CommonUserDailyVisit::getId);
            return wrapper;
        }
        WordUserIdStrSupport.applyIdStrToUserId(request);
        if (request.getId() != null) {
            wrapper.eq(CommonUserDailyVisit::getId, request.getId());
        }
        if (request.getUserId() != null) {
            wrapper.eq(CommonUserDailyVisit::getUserId, request.getUserId());
        }
        if (request.getVisitDate() != null) {
            wrapper.eq(CommonUserDailyVisit::getVisitDate, request.getVisitDate());
        }
        if (request.getVisitDateFrom() != null) {
            wrapper.ge(CommonUserDailyVisit::getVisitDate, request.getVisitDateFrom());
        }
        if (request.getVisitDateTo() != null) {
            wrapper.le(CommonUserDailyVisit::getVisitDate, request.getVisitDateTo());
        }
        wrapper.orderByDesc(CommonUserDailyVisit::getVisitDate)
            .orderByDesc(CommonUserDailyVisit::getVisitCount)
            .orderByDesc(CommonUserDailyVisit::getId);
        return wrapper;
    }

    @Override
    protected AdminCommonUserDailyVisitResponse toResponse(CommonUserDailyVisit entity) {
        AdminCommonUserDailyVisitResponse res = super.toResponse(entity);
        WordUserIdStrSupport.fillIdStr(res);
        return res;
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        List<Long> unique = new ArrayList<>();
        Set<Long> seen = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id != null && seen.add(id)) {
                unique.add(id);
            }
        }
        int rows = 0;
        for (Long id : unique) {
            rows += mapper.deleteById(id);
        }
        return rows;
    }
}
