package com.arelore.server.core.user.dto;

import lombok.Data;

/**
 * 用户查询请求 DTO
 */
@Data
public class UserQueryRequest {

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
}
