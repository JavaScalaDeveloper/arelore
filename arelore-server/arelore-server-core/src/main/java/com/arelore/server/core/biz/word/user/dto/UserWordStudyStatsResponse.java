package com.arelore.server.core.biz.word.user.dto;

import lombok.Data;

/**
 * 个人中心学习统计：累计学习天数、连续学习天数（自昨日往前）。
 */
@Data
public class UserWordStudyStatsResponse {
    /** 累计有过学习行为的天数 */
    private int totalStudyDays;
    /**
     * 连续学习天数：从昨日起往前连续有学习的天数；
     * 昨日未学则为 0，中间断档则截断。
     */
    private int consecutiveStudyDays;
}
