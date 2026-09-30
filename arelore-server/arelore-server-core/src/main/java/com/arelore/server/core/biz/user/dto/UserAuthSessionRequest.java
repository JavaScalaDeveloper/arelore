package com.arelore.server.core.biz.user.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserAuthSessionRequest extends UserAuthSessionResponse {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
