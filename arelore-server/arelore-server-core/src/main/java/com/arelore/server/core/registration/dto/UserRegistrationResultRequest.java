package com.arelore.server.core.registration.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserRegistrationResultRequest extends UserRegistrationResultResponse {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}

