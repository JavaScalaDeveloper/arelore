package com.arelore.server.core.biz.word.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_word_study_plan")
public class UserWordStudyPlan {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("modify_time")
    private LocalDateTime modifyTime;

    @TableField("user_id")
    private BigDecimal userId;

    @TableField("book_code")
    private String bookCode;

    @TableField("new_review_ratio")
    private String newReviewRatio;

    @TableField("daily_new_count")
    private Integer dailyNewCount;

    @TableField("daily_review_count")
    private Integer dailyReviewCount;

    @TableField("plan_days")
    private Integer planDays;

    @TableField("ext_info")
    private String extInfo;
}
