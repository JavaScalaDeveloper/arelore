package com.arelore.server.core.biz.prompt.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("arelore_creator.user_ai_prompt_pref")
public class UserAiPromptPref {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("modify_time")
    private LocalDateTime modifyTime;

    @TableField("user_id")
    private BigDecimal userId;

    @TableField("status")
    private String status;

    /** 偏好整包 JSON：fontScale/fontSizeSp/copyFields/copySep 等 */
    @TableField("ext_json")
    private String extJson;
}
