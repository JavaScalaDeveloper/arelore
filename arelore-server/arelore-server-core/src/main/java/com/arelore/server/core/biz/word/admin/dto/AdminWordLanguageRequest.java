package com.arelore.server.core.biz.word.admin.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminWordLanguageRequest extends AdminWordLanguageResponse {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
