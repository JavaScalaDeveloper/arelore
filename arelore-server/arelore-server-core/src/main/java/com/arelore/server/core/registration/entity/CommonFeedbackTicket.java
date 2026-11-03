package com.arelore.server.core.registration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 通用意见反馈工单（arelore_common.common_feedback_ticket）。
 */
@Data
@TableName("arelore_common.common_feedback_ticket")
public class CommonFeedbackTicket {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("modify_time")
    private LocalDateTime modifyTime;

    @TableField("user_id")
    private BigDecimal userId;

    @TableField("product_code")
    private String productCode;

    @TableField("title")
    private String title;

    /** 1待处理 2处理中 3待用户回复 4已关闭 */
    @TableField("status")
    private Integer status;

    @TableField("admin_user_id")
    private BigDecimal adminUserId;

    @TableField("ext_info")
    private String extInfo;
}
