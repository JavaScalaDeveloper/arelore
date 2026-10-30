package com.arelore.server.core.biz.prompt.user;

import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptDailyVisitRequest;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptDailyVisitResponse;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptVisitStatsResponse;
import com.arelore.server.core.biz.prompt.user.entity.UserAiPromptDailyVisit;
import com.arelore.server.core.biz.prompt.user.mapper.UserAiPromptDailyVisitMapper;
import com.arelore.server.core.biz.word.user.support.WordUserIdStrSupport;
import com.arelore.server.core.common.time.ChinaTime;
import com.arelore.server.core.service.BaseServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
public class UserAiPromptDailyVisitServiceImpl
    extends BaseServiceImpl<UserAiPromptDailyVisitRequest, UserAiPromptDailyVisitResponse, UserAiPromptDailyVisit>
    implements UserAiPromptDailyVisitService {

    private static final Logger log = LoggerFactory.getLogger(UserAiPromptDailyVisitServiceImpl.class);

    public static final String CLIENT_ANDROID = "ANDROID";
    public static final String CLIENT_WEB = "WEB";
    public static final String CLIENT_MINI = "MINI";
    public static final String CLIENT_UNKNOWN = "UNKNOWN";

    private final UserAiPromptDailyVisitMapper mapper;

    public UserAiPromptDailyVisitServiceImpl(UserAiPromptDailyVisitMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected UserAiPromptDailyVisitMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<UserAiPromptDailyVisitResponse> responseClass() {
        return UserAiPromptDailyVisitResponse.class;
    }

    @Override
    protected LambdaQueryWrapper<UserAiPromptDailyVisit> buildWrapper(UserAiPromptDailyVisitRequest request) {
        WordUserIdStrSupport.applyIdStrToUserId(request);
        LambdaQueryWrapper<UserAiPromptDailyVisit> wrapper = new LambdaQueryWrapper<>();
        if (request == null) {
            wrapper.orderByDesc(UserAiPromptDailyVisit::getVisitDate)
                .orderByDesc(UserAiPromptDailyVisit::getId);
            return wrapper;
        }
        if (request.getId() != null) {
            wrapper.eq(UserAiPromptDailyVisit::getId, request.getId());
        }
        if (request.getUserId() != null) {
            wrapper.eq(UserAiPromptDailyVisit::getUserId, request.getUserId());
        }
        if (request.getVisitDate() != null) {
            wrapper.eq(UserAiPromptDailyVisit::getVisitDate, request.getVisitDate());
        }
        if (request.getVisitDateFrom() != null) {
            wrapper.ge(UserAiPromptDailyVisit::getVisitDate, request.getVisitDateFrom());
        }
        if (request.getVisitDateTo() != null) {
            wrapper.le(UserAiPromptDailyVisit::getVisitDate, request.getVisitDateTo());
        }
        if (StringUtils.hasText(request.getClient())) {
            wrapper.eq(UserAiPromptDailyVisit::getClient, normalizeClient(request.getClient()));
        }
        wrapper.orderByDesc(UserAiPromptDailyVisit::getVisitDate)
            .orderByDesc(UserAiPromptDailyVisit::getVisitCount)
            .orderByDesc(UserAiPromptDailyVisit::getId);
        return wrapper;
    }

    @Override
    protected UserAiPromptDailyVisitResponse toResponse(UserAiPromptDailyVisit entity) {
        UserAiPromptDailyVisitResponse response = super.toResponse(entity);
        WordUserIdStrSupport.fillIdStr(response);
        return response;
    }

    @Override
    public void recordVisit(BigDecimal userId, String client) {
        if (userId == null) {
            return;
        }
        try {
            mapper.upsertVisit(userId, ChinaTime.nowDate(), normalizeClient(client));
        } catch (Exception e) {
            log.warn("record prompt daily visit failed, userId={}", userId.toPlainString(), e);
        }
    }

    @Override
    public List<UserAiPromptVisitStatsResponse> statsByDay(UserAiPromptDailyVisitRequest request) {
        if (request == null) {
            return Collections.emptyList();
        }
        String client = StringUtils.hasText(request.getClient())
            ? normalizeClient(request.getClient())
            : null;
        List<UserAiPromptVisitStatsResponse> list = mapper.statsByDay(
            request.getVisitDateFrom(),
            request.getVisitDateTo(),
            client
        );
        return list == null ? Collections.emptyList() : list;
    }

    public static String normalizeClient(String client) {
        if (!StringUtils.hasText(client)) {
            return CLIENT_UNKNOWN;
        }
        String code = client.trim().toUpperCase(Locale.ROOT);
        return switch (code) {
            case CLIENT_ANDROID, CLIENT_WEB, CLIENT_MINI -> code;
            default -> CLIENT_UNKNOWN;
        };
    }
}
