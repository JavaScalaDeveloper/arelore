package com.arelore.server.user.dto;

import lombok.Data;

/**
 * 用户删除请求 DTO
 */
@Data
public class UserDeleteRequest {

    /**
     * 用户 ID（必填）
     */
    private String id;
}
