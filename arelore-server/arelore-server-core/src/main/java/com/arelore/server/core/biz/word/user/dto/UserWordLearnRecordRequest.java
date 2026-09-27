package com.arelore.server.core.biz.word.user.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserWordLearnRecordRequest extends UserWordLearnRecordResponse {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
