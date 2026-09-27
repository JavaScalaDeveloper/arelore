package com.arelore.server.core.biz.word.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_word_book")
public class AdminWordBook {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("modify_time")
    private LocalDateTime modifyTime;

    @TableField("code")
    private String code;

    @TableField("name")
    private String name;

    @TableField("description")
    private String description;

    @TableField("word_count")
    private Integer wordCount;

    @TableField("language_code")
    private String languageCode;

    @TableField("category_code")
    private String categoryCode;

    @TableField("status")
    private Integer status;

    @TableField("ext_info")
    private String extInfo;
}
