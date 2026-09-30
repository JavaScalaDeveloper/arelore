package com.arelore.server.core.biz.word.user.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserWordStudyPlanRequest extends UserWordStudyPlanResponse {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
