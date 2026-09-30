package com.arelore.server.core.biz.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arelore.server.core.service.BaseServiceImpl;
import com.arelore.server.core.registration.dto.UserRegistrationResultRequest;
import com.arelore.server.core.registration.dto.UserRegistrationResultResponse;
import com.arelore.server.core.registration.entity.UserRegistrationResult;
import com.arelore.server.core.registration.mapper.UserRegistrationResultMapper;
import com.arelore.server.core.biz.user.UserRegistrationResultService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserRegistrationResultServiceImpl
    extends BaseServiceImpl<UserRegistrationResultRequest, UserRegistrationResultResponse, UserRegistrationResult>
    implements UserRegistrationResultService {

    private final UserRegistrationResultMapper mapper;

    public UserRegistrationResultServiceImpl(UserRegistrationResultMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected UserRegistrationResultMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<UserRegistrationResultResponse> responseClass() {
        return UserRegistrationResultResponse.class;
    }

    @Override
    protected UserRegistrationResultResponse toResponse(UserRegistrationResult entity) {
        UserRegistrationResultResponse response = super.toResponse(entity);
        fillIdStr(response);
        return response;
    }

    @Override
    protected LambdaQueryWrapper<UserRegistrationResult> buildWrapper(UserRegistrationResultRequest request) {
        LambdaQueryWrapper<UserRegistrationResult> w = new LambdaQueryWrapper<>();
        if (request == null) {
            return w;
        }
        if (StringUtils.hasText(request.getAccountType())) {
            w.eq(UserRegistrationResult::getAccountType, request.getAccountType());
        }
        if (StringUtils.hasText(request.getAccount())) {
            w.eq(UserRegistrationResult::getAccount, request.getAccount());
        }
        if (request.getUserId() != null) {
            w.eq(UserRegistrationResult::getUserId, request.getUserId());
        }
        if (StringUtils.hasText(request.getIdStr())) {
            try {
                w.eq(UserRegistrationResult::getUserId, new java.math.BigDecimal(request.getIdStr().trim()));
            } catch (NumberFormatException ignored) {
                w.eq(UserRegistrationResult::getId, -1L);
            }
        }
        w.orderByDesc(UserRegistrationResult::getId);
        return w;
    }

    private static void fillIdStr(UserRegistrationResultResponse response) {
        if (response == null || response.getUserId() == null) {
            return;
        }
        response.setIdStr(response.getUserId().toPlainString());
    }
}

