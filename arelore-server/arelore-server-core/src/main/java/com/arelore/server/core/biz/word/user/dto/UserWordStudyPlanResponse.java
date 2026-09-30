package com.arelore.server.core.biz.word.user.dto;

import com.arelore.server.core.biz.word.user.entity.UserWordStudyPlan;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserWordStudyPlanResponse extends UserWordStudyPlan {
    private String bookName;
    private Integer wordCount;
    /** 用户业务 ID 字符串（对应 userId），避免前端精度丢失 */
    private String idStr;
}
