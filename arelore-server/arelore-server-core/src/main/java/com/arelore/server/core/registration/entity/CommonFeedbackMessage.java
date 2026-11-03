package com.arelore.server.core.registration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 意见反馈工单消息（arelore_common.common_feedback_message）。
 * 表无 create_time，展示/排序用 modify_time 与 id。
 */
@Data
@TableName("arelore_common.common_feedback_message")
public class CommonFeedbackMessage {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("modify_time")
    private LocalDateTime modifyTime;

    @TableField("ticket_id")
    private Long ticketId;

    /** USER / ADMIN */
    @TableField("sender_type")
    private String senderType;

    @TableField("sender_user_id")
    private BigDecimal senderUserId;

    @TableField("content_text")
    private String contentText;

    @TableField("ext_info")
    private String extInfo;
}
