package com.arelore.server.core.biz.word.admin.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminWordEntryRequest extends AdminWordEntryResponse {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
