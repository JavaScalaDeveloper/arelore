package com.arelore.server.core.biz.word.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_word_learn_record")
public class UserWordLearnRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("modify_time")
    private LocalDateTime modifyTime;

    @TableField("user_id")
    private BigDecimal userId;

    @TableField("book_code")
    private String bookCode;

    @TableField("word_code")
    private String wordCode;

    @TableField("ext_info")
    private String extInfo;
}
