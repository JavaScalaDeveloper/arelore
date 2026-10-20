package com.arelore.server.core.registration.mail;

import com.arelore.server.core.common.exception.BusinessException;
import com.arelore.server.core.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 未启用邮件配置时的占位实现：拒绝发信并打日志，避免静默失败。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "arelore.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoopEmailSender implements EmailSender {

    @Override
    public void sendVerifyCode(String email, String code, int validMinutes) {
        log.warn("arelore.mail.enabled=false，跳过真实发信。email={}, code={}, minutes={}", email, code, validMinutes);
        throw new BusinessException(ResultCode.USER_REGISTER_APPLY_FAILED, "邮件服务未配置，请联系管理员");
    }
}
