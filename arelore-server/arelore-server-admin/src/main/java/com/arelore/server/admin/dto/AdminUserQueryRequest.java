package com.arelore.server.admin.dto;

import lombok.Data;

/**
 * 管理员查询用户请求 DTO
 */
@Data
public class AdminUserQueryRequest {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 用户名（可选）
     */
    private String username;

    /**
     * 邮箱（可选）
     */
    private String email;

    /**
     * 状态：true-启用，false-禁用（可选）
     */
    private Boolean status;
}
