package com.arelore.server.core.biz.word.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_word_current_book")
public class UserWordCurrentBook {
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

    @TableField("learn_done")
    private Integer learnDone;

    @TableField("learn_todo")
    private Integer learnTodo;

    @TableField("review_done")
    private Integer reviewDone;

    @TableField("review_todo")
    private Integer reviewTodo;

    @TableField("ext_info")
    private String extInfo;
}
