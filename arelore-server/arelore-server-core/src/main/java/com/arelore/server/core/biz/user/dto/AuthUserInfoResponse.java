package com.arelore.server.core.biz.user.dto;

import lombok.Data;

/**
 * 认证域通用用户信息。
 */
@Data
public class AuthUserInfoResponse {
    /**
     * 用户唯一标识。
     */
    private String id;

    /**
     * 用户名（当前移动端场景通常为手机号或微信昵称）。
     */
    private String username;

    /**
     * 前端展示昵称。
     */
    private String nickname;

    /**
     * 头像地址，可为空。
     */
    private String avatar;
}

