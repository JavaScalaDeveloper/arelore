package com.arelore.server.core.user.dto;

import lombok.Data;

/**
 * 用户创建/更新请求 DTO
 */
@Data
public class UserSaveRequest {

    /**
     * 用户 ID（更新时必填）
     */
    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 密码（创建时必填）
     */
    private String password;

    /**
     * 状态：true-启用，false-禁用
     */
    private Boolean status = true;
}
