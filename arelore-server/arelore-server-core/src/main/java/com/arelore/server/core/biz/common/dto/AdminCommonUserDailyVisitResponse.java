package com.arelore.server.core.biz.common.dto;

import com.arelore.server.core.registration.entity.CommonUserDailyVisit;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端日访问统计 VO。idStr 对应 userId，防前端精度丢失。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminCommonUserDailyVisitResponse extends CommonUserDailyVisit {
    /** 用户业务 ID 字符串（对应 userId） */
    private String idStr;
}
