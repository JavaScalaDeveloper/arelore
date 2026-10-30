package com.arelore.server.core.biz.prompt.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("arelore_creator.user_ai_prompt_daily_visit")
public class UserAiPromptDailyVisit {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("modify_time")
    private LocalDateTime modifyTime;

    @TableField("user_id")
    private BigDecimal userId;

    @TableField("visit_date")
    private LocalDate visitDate;

    @TableField("visit_count")
    private Integer visitCount;

    @TableField("client")
    private String client;

    @TableField("ext_json")
    private String extJson;
}
