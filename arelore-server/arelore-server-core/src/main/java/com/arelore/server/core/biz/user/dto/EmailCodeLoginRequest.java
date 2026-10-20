package com.arelore.server.core.biz.user.dto;

import lombok.Data;

/**
 * 邮箱验证码登录。
 */
@Data
public class EmailCodeLoginRequest {

    /**
     * 登录邮箱。
     */
    private String email;

    /**
     * 邮件验证码。
     */
    private String code;
}
