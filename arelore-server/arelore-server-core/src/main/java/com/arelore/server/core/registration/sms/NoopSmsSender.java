package com.arelore.server.core.registration.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 短信发送兜底实现：用于未开启阿里云短信时保证服务可启动。
 * 生产环境请开启 aliyun.sms.enabled=true 并使用 AliyunSmsSender。
 */
public class NoopSmsSender implements SmsSender {
    private static final Logger log = LoggerFactory.getLogger(NoopSmsSender.class);

    @Override
    public void sendVerifyCode(String phoneNumber, String code, int validMinutes) {
        // 不抛异常，避免影响注册申请链路；验证码已写入 application.ext_info，可用于测试环境验证
        log.warn("短信未启用，已生成验证码但未发送。phone={}, code={}, validMinutes={}", phoneNumber, code, validMinutes);
    }
}

