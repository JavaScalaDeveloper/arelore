package com.arelore.server.core.registration.dto;

import com.arelore.server.core.registration.entity.UserRegistrationResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserRegistrationResultResponse extends UserRegistrationResult {
    /**
     * 业务用户 ID 字符串（对应 userId）。
     * 前端请用本字段展示与查询，避免 Number 精度丢失。
     */
    private String idStr;
}
