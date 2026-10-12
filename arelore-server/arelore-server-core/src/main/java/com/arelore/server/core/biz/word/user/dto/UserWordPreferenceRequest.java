package com.arelore.server.core.biz.word.user.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserWordPreferenceRequest extends UserWordPreferenceResponse {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
