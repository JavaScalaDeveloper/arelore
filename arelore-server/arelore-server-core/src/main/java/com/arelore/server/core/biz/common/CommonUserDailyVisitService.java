package com.arelore.server.core.biz.common;

import java.math.BigDecimal;

/**
 * C 端日访问累加（供拦截器异步调用，失败不影响业务）。
 */
public interface CommonUserDailyVisitService {
    /** 为指定用户累加「今天」访问次数 1 */
    void recordVisit(BigDecimal userId);
}
