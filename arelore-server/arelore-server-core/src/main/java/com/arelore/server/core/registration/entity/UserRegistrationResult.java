package com.arelore.server.core.registration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_registration_result")
public class UserRegistrationResult {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("modify_time")
    private LocalDateTime modifyTime;

    @TableField("user_id")
    private BigDecimal userId;

    @TableField("merged_to_user_id")
    private BigDecimal mergedToUserId;

    @TableField("account_type")
    private String accountType;

    @TableField("account")
    private String account;

    @TableField("status")
    private Integer status;

    @TableField("ext_info")
    private String extInfo;
}

