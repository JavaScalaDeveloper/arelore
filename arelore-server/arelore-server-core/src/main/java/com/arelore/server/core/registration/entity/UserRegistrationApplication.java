package com.arelore.server.core.registration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_registration_application")
public class UserRegistrationApplication {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("modify_time")
    private LocalDateTime modifyTime;

    @TableField("account_type")
    private String accountType;

    @TableField("account")
    private String account;

    @TableField("client_ip")
    private String clientIp;

    @TableField("ext_info")
    private String extInfo;
}

