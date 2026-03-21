package com.arelore.server.admin.dto;

import lombok.Data;

/**
 * 管理员登录请求 DTO
 */
@Data
public class AdminLoginRequest {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
