package com.arelore.server.core.biz.user.dto;

import lombok.Data;

/**
 * 邮箱注册确认：验证码 + 密码摘要（前端 sha256(明文)）。
 */
@Data
public class EmailRegisterConfirmRequest {

    private String email;

    private String code;

    /**
     * 密码摘要：前端 sha256(明文密码) 的十六进制，禁止传明文。
     */
    private String passwordHash;
}
