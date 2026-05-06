package com.arelore.server.core.detection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_detection_question_favorite")
public class UserDetectionQuestionFavorite {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("user_id")
    private String userId;

    @TableField("question_type_code")
    private String questionTypeCode;

    @TableField("question_code")
    private String questionCode;

    @TableField("extra_info")
    private String extraInfo;
}
