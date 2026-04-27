package com.arelore.server.core.registration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("arelore_common.common_binary_file")
public class CommonBinaryFile {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("modify_time")
    private LocalDateTime modifyTime;

    @TableField("hash_value")
    private byte[] hashValue;

    @TableField("file_data")
    private byte[] fileData;

    @TableField("status")
    private Integer status;

    @TableField("ext_json")
    private String extJson;
}

