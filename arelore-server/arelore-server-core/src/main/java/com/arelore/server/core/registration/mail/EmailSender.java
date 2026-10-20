package com.arelore.server.core.registration.mail;

/**
 * 邮箱验证码发送通道。
 */
public interface EmailSender {

    /**
     * 发送登录/注册验证码邮件。
     *
     * @param email        收件邮箱
     * @param code         验证码
     * @param validMinutes 有效分钟数
     */
    void sendVerifyCode(String email, String code, int validMinutes);
}
