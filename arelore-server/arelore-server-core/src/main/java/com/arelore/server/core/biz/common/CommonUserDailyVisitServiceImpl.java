package com.arelore.server.core.biz.common;

import com.arelore.server.core.common.time.ChinaTime;
import com.arelore.server.core.registration.mapper.CommonUserDailyVisitMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class CommonUserDailyVisitServiceImpl implements CommonUserDailyVisitService {
    private static final Logger log = LoggerFactory.getLogger(CommonUserDailyVisitServiceImpl.class);

    private final CommonUserDailyVisitMapper mapper;

    public CommonUserDailyVisitServiceImpl(CommonUserDailyVisitMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void recordVisit(BigDecimal userId) {
        if (userId == null) {
            return;
        }
        try {
            LocalDate today = ChinaTime.nowDate();
            mapper.upsertVisit(userId, today);
        } catch (Exception e) {
            // 统计失败绝不向上抛，避免影响业务请求
            log.warn("record daily visit failed, userId={}", userId.toPlainString(), e);
        }
    }
}
