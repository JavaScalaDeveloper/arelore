package com.arelore.server.core.registration.mail;

import com.arelore.server.core.common.exception.BusinessException;
import com.arelore.server.core.common.result.ResultCode;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 基于 Spring JavaMailSender 的 SMTP 发信（个人 163 等）。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "arelore.mail", name = "enabled", havingValue = "true")
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpEmailSender(
        JavaMailSender mailSender,
        @Value("${arelore.mail.from:}") String from,
        @Value("${spring.mail.username:}") String username
    ) {
        this.mailSender = mailSender;
        if (StringUtils.hasText(from)) {
            this.from = from.trim();
        } else if (StringUtils.hasText(username)) {
            this.from = username.trim();
        } else {
            this.from = "";
        }
    }

    @Override
    public void sendVerifyCode(String email, String code, int validMinutes) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(code)) {
            throw new BusinessException(ResultCode.USER_REGISTER_PARAM_INVALID, "邮箱或验证码不能为空");
        }
        if (!StringUtils.hasText(from)) {
            throw new BusinessException(ResultCode.USER_REGISTER_APPLY_FAILED, "发件邮箱未配置");
        }
        int minutes = validMinutes > 0 ? validMinutes : 5;
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(email.trim());
            helper.setSubject("【Arelore】登录验证码");
            helper.setText(
                "您的验证码是 " + code + "，" + minutes + " 分钟内有效。\n\n如非本人操作，请忽略本邮件。",
                false
            );
            mailSender.send(message);
            log.info("邮箱验证码已发送，to={}", email);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("SMTP 发送验证码失败，to={}, message={}", email, e.getMessage(), e);
            throw new BusinessException(ResultCode.USER_REGISTER_APPLY_FAILED, "验证码发送失败，请稍后重试");
        }
    }
}
