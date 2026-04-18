package com.arelore.server.core.registration.dto;

import lombok.Data;

/**
 * 手机号找回密码-确认重置请求。
 */
@Data
public class MobileResetPasswordConfirmRequest {
    /**
     * 手机号
     */
    private String mobile;

    /**
     * 短信验证码
     */
    private String verifyCode;

    /**
     * 新密码（可传明文或前端摘要值，后端统一加盐哈希保存）
     */
    private String newPassword;
}
