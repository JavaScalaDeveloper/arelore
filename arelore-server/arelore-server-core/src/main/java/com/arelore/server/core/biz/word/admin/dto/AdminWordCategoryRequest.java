package com.arelore.server.core.biz.word.admin.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminWordCategoryRequest extends AdminWordCategoryResponse {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
