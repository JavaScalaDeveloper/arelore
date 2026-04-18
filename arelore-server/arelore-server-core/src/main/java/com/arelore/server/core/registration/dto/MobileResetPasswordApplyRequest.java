package com.arelore.server.core.registration.dto;

import lombok.Data;

/**
 * 手机号找回密码-发送验证码请求。
 */
@Data
public class MobileResetPasswordApplyRequest {
    /**
     * 手机号
     */
    private String mobile;
}
