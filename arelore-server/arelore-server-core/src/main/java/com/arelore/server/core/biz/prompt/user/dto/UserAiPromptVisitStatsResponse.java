package com.arelore.server.core.biz.prompt.user.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 提示词 App 按日 PV/UV 汇总（非表实体 VO）。
 */
@Data
public class UserAiPromptVisitStatsResponse {
    private LocalDate visitDate;
    private String client;
    /** 当日 PV = SUM(visit_count) */
    private Long pv;
    /** 当日 UV = 有访问记录的用户数 */
    private Long uv;
}
