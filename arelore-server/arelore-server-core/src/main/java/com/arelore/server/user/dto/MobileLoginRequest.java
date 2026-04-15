package com.arelore.server.user.dto;

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
}
