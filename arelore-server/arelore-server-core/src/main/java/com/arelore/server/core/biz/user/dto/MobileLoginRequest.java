package com.arelore.server.core.biz.user.dto;

import lombok.Data;

/**
 * 移动端账号密码登录请求
 */
@Data
public class MobileLoginRequest {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 前端 SHA-256 后的密码摘要（推荐字段，不传明文）。
     */
    private String passwordHash;
}
