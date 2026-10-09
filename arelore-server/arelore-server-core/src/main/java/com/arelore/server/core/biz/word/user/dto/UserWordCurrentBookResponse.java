package com.arelore.server.core.biz.word.user.dto;

import com.arelore.server.core.biz.word.user.entity.UserWordCurrentBook;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserWordCurrentBookResponse extends UserWordCurrentBook {
    private String bookName;
    private String bookDescription;
    private Integer wordCount;
    /** 用户业务 ID 字符串（对应 userId），避免前端精度丢失 */
    private String idStr;
    /** 已学词数（有学习记录） */
    private Integer learnedCount;
    /** 计划预计完成天数 */
    private Integer planDays;
    /** 按每日新学量估算的剩余天数 */
    private Integer remainingDays;
    /** 整书进度 0~100 */
    private Integer progressPercent;
}
