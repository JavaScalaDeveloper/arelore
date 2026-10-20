package com.arelore.server.core.biz.user.dto;

import lombok.Data;

/**
 * 邮箱 + 密码摘要登录。
 */
@Data
public class EmailPasswordLoginRequest {

    /**
     * 邮箱（账号）。
     */
    private String email;

    /**
     * 密码摘要：前端 sha256(明文密码) 的十六进制，禁止传明文。
     */
    private String passwordHash;
}
