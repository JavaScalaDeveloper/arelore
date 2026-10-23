package com.arelore.server.core.registration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * C 端用户日访问统计（arelore_common.common_user_daily_visit）。
 * 表结构以已执行 DDL 为准：无 create_time。
 */
@Data
@TableName("arelore_common.common_user_daily_visit")
public class CommonUserDailyVisit {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("modify_time")
    private LocalDateTime modifyTime;

    @TableField("user_id")
    private BigDecimal userId;

    @TableField("visit_date")
    private LocalDate visitDate;

    @TableField("visit_count")
    private Integer visitCount;

    @TableField("ext_info")
    private String extInfo;
}
