package com.arelore.server.core.service.cert.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arelore.server.core.service.BaseServiceImpl;
import com.arelore.server.core.detection.dto.UserDetectResultHistoryRequest;
import com.arelore.server.core.detection.dto.UserDetectResultHistoryResponse;
import com.arelore.server.core.detection.entity.UserDetectResultHistory;
import com.arelore.server.core.detection.mapper.UserDetectResultHistoryMapper;
import com.arelore.server.core.service.cert.user.UserDetectResultHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserDetectResultHistoryServiceImpl
    extends BaseServiceImpl<UserDetectResultHistoryRequest, UserDetectResultHistoryResponse, UserDetectResultHistory>
    implements UserDetectResultHistoryService {

    private final UserDetectResultHistoryMapper mapper;

    public UserDetectResultHistoryServiceImpl(UserDetectResultHistoryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected UserDetectResultHistoryMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<UserDetectResultHistoryResponse> responseClass() {
        return UserDetectResultHistoryResponse.class;
    }

    @Override
    protected LambdaQueryWrapper<UserDetectResultHistory> buildWrapper(UserDetectResultHistoryRequest request) {
        LambdaQueryWrapper<UserDetectResultHistory> w = new LambdaQueryWrapper<>();
        if (request == null) {
            return w;
        }
        if (StringUtils.hasText(request.getUserId())) {
            w.eq(UserDetectResultHistory::getUserId, request.getUserId());
        }
        if (StringUtils.hasText(request.getUserDetectTypeCode())) {
            w.eq(UserDetectResultHistory::getUserDetectTypeCode, request.getUserDetectTypeCode());
        }
        if (StringUtils.hasText(request.getUserDetectResult())) {
            w.eq(UserDetectResultHistory::getUserDetectResult, request.getUserDetectResult());
        }
        w.orderByDesc(UserDetectResultHistory::getCreateTime).orderByDesc(UserDetectResultHistory::getId);
        return w;
    }
}

