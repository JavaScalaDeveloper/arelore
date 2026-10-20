package com.arelore.server.core.biz.user.dto;

import lombok.Data;

/**
 * 申请邮箱登录验证码。
 */
@Data
public class EmailCodeApplyRequest {

    /**
     * 登录邮箱。
     */
    private String email;
}
