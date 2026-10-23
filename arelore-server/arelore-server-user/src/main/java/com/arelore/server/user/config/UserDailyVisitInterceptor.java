package com.arelore.server.user.config;

import com.arelore.server.core.biz.common.CommonUserDailyVisitService;
import com.arelore.server.core.biz.user.AuthService;
import com.arelore.server.core.biz.user.dto.AuthUserInfoResponse;
import com.arelore.server.core.registration.entity.UserRegistrationResult;
import com.arelore.server.core.registration.enums.AccountTypeEnum;
import com.arelore.server.core.registration.mapper.UserRegistrationResultMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * C 端已登录请求：异步累加日访问，异常全部吞掉，不影响业务链路。
 */
@Component
public class UserDailyVisitInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(UserDailyVisitInterceptor.class);

    private final AuthService authService;
    private final UserRegistrationResultMapper registrationResultMapper;
    private final CommonUserDailyVisitService visitService;
    private final ExecutorService executor = new ThreadPoolExecutor(
        1,
        2,
        60L,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(2000),
        r -> {
            Thread t = new Thread(r, "user-daily-visit");
            t.setDaemon(true);
            return t;
        },
        new ThreadPoolExecutor.DiscardOldestPolicy()
    );

    public UserDailyVisitInterceptor(
        AuthService authService,
        UserRegistrationResultMapper registrationResultMapper,
        CommonUserDailyVisitService visitService
    ) {
        this.authService = authService;
        this.registrationResultMapper = registrationResultMapper;
        this.visitService = visitService;
    }

    @Override
    public void afterCompletion(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        Exception ex
    ) {
        try {
            String token = request.getHeader("Authorization");
            if (!StringUtils.hasText(token)) {
                return;
            }
            final String authToken = token.trim();
            executor.execute(() -> {
                try {
                    BigDecimal userId = resolveUserId(authToken);
                    if (userId != null) {
                        visitService.recordVisit(userId);
                    }
                } catch (Exception e) {
                    log.debug("async daily visit skipped: {}", e.toString());
                }
            });
        } catch (Exception e) {
            log.debug("daily visit schedule skipped: {}", e.toString());
        }
    }

    private BigDecimal resolveUserId(String token) {
        AuthUserInfoResponse user = authService.getCurrentUser(token);
        if (user == null || !StringUtils.hasText(user.getId())) {
            return null;
        }
        String account = user.getId().trim();
        try {
            return new BigDecimal(account);
        } catch (NumberFormatException ignored) {
            // 微信 openid → 注册表 user_id
        }
        LambdaQueryWrapper<UserRegistrationResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRegistrationResult::getAccountType, AccountTypeEnum.WECHAT.code())
            .eq(UserRegistrationResult::getAccount, account)
            .orderByDesc(UserRegistrationResult::getId)
            .last("limit 1");
        UserRegistrationResult result = registrationResultMapper.selectOne(wrapper);
        return result == null ? null : result.getUserId();
    }
}
