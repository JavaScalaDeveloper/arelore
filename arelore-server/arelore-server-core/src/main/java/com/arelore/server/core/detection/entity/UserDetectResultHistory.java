package com.arelore.server.core.detection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_detect_result_history")
public class UserDetectResultHistory {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("modify_time")
    private LocalDateTime modifyTime;

    @TableField("user_id")
    private String userId;

    @TableField("user_detect_type_code")
    private String userDetectTypeCode;

    @TableField("user_detect_result")
    private String userDetectResult;

    @TableField("extra_info")
    private String extraInfo;
}
