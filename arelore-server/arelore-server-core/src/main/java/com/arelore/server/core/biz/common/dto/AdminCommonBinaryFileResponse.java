package com.arelore.server.core.biz.common.dto;

import com.arelore.server.core.registration.entity.CommonBinaryFile;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端二进制文件 VO。列表不带 fileData；详情用 base64Data。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"fileData", "hashValue"})
public class AdminCommonBinaryFileResponse extends CommonBinaryFile {
    /** 哈希十六进制 */
    private String hashHex;
    private String contentType;
    private String source;
    /** 字节数（来自 ext_json.fileSize，或详情时按 file_data 回退） */
    private Long fileSize;
    /** 人类可读大小，如 128.5 KB */
    private String fileSizeText;
    private Integer refCount;
    /** 详情才填：Base64 内容，供预览 */
    private String base64Data;
}
