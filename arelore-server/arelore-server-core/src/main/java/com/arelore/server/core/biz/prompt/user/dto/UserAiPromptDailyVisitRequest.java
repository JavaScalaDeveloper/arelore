package com.arelore.server.core.biz.prompt.user.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserAiPromptDailyVisitRequest extends UserAiPromptDailyVisitResponse {
    private Integer pageNum = 1;
    private Integer pageSize = 10;

    /** 访问日起（含） */
    private LocalDate visitDateFrom;
    /** 访问日止（含） */
    private LocalDate visitDateTo;
}
