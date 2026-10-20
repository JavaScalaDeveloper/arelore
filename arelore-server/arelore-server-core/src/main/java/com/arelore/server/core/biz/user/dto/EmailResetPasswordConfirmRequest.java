package com.arelore.server.core.biz.user.dto;

import lombok.Data;

/**
 * 邮箱找回/设置密码确认（新密码为前端摘要）。
 */
@Data
public class EmailResetPasswordConfirmRequest {

    private String email;

    private String code;

    /**
     * 新密码摘要：前端 sha256(明文新密码) 的十六进制，禁止传明文。
     */
    private String newPasswordHash;
}
