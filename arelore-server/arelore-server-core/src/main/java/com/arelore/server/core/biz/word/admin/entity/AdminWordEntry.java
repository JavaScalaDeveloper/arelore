package com.arelore.server.core.biz.word.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_word_entry")
public class AdminWordEntry {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("modify_time")
    private LocalDateTime modifyTime;

    @TableField("book_code")
    private String bookCode;

    @TableField("word_code")
    private String wordCode;

    @TableField("word")
    private String word;

    @TableField("sort_no")
    private Integer sortNo;

    @TableField("ext_info")
    private String extInfo;
}
