package com.arelore.server.core.biz.prompt.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("arelore_creator.user_ai_prompt")
public class UserAiPrompt {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("modify_time")
    private LocalDateTime modifyTime;

    @TableField("user_id")
    private BigDecimal userId;

    @TableField("prompt_uuid")
    private String promptUuid;

    @TableField("title")
    private String title;

    @TableField("prompt_text")
    private String promptText;

    /** 配置标签 JSON 数组，可手打也可被 CSV 导入覆盖 */
    @TableField("config_tags")
    private String configTags;

    /** 人工标签 JSON 数组，导入不覆盖 */
    @TableField("manual_tags")
    private String manualTags;

    @TableField("publish_status")
    private String publishStatus;

    @TableField("status")
    private String status;

    @TableField("ext_info")
    private String extInfo;
}
