package com.arelore.server.core.biz.user.dto;

import lombok.Data;

/**
 * 统一的登录响应对象。
 */
@Data
public class AuthLoginResponse {
    /**
     * 登录凭证，格式通常为 Bearer xxx。
     */
    private String token;

    /**
     * 当前登录用户信息。
     */
    private AuthUserInfoResponse user;
}

