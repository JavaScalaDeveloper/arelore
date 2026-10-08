package com.arelore.server.core.biz.word.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_word_base_info")
public class AdminWordBaseInfo {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("modify_time")
    private LocalDateTime modifyTime;

    @TableField("language_code")
    private String languageCode;

    @TableField("word")
    private String word;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @TableField("ext_info")
    private String extInfo;
}
