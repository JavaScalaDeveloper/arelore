package com.arelore.server.core.biz.user.dto;

import lombok.Data;

/**
 * 小程序“手册登录”请求体（手机号+密码摘要）。
 */
@Data
public class MiniManualLoginRequest {
    /**
     * 手机号
     */
    private String mobile;

    /**
     * 密码摘要（建议前端 sha256(明文) 后传输，避免明文）
     */
    private String passwordHash;
}

