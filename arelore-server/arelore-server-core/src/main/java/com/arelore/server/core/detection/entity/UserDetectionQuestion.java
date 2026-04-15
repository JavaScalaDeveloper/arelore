package com.arelore.server.core.detection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_detection_question")
public class UserDetectionQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("modify_time")
    private LocalDateTime modifyTime;

    @TableField("modifier")
    private String modifier;

    @TableField("type_code")
    private String typeCode;

    @TableField("question_code")
    private String questionCode;

    @TableField("question_name")
    private String questionName;

    @TableField("question_order")
    private Integer questionOrder;

    @TableField("question_description")
    private String questionDescription;

    @TableField("options")
    private String options;

    @TableField("extra_info")
    private String extraInfo;
}
