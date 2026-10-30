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

    /**
     * 对应列 {@code hash_value BINARY(32)}：SHA-256 原始字节，不是 hex 字符串。
     * 业务 JSON（如 meaningAudio.hashValue）存的是 64 位 hex；SQL 按内容查须：
     * {@code WHERE hash_value = UNHEX('…hex…')}，或 {@code WHERE id = fileId}。
     */
    @TableField("hash_value")
    private byte[] hashValue;

    @TableField("file_data")
    private byte[] fileData;

    @TableField("status")
    private Integer status;

    @TableField("ext_json")
    private String extJson;
}

