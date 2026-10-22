package com.arelore.server.core.biz.common.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminCommonBinaryFileRequest extends AdminCommonBinaryFileResponse {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    /** 按 source 模糊/精确：PIXABAY / QWEN3_TTS 等 */
    private String sourceFilter;

    private LocalDateTime createTimeFrom;
    private LocalDateTime createTimeTo;
    private LocalDateTime modifyTimeFrom;
    private LocalDateTime modifyTimeTo;

    /** 文件大小下限（字节，含） */
    private Long fileSizeMin;
    /** 文件大小上限（字节，含） */
    private Long fileSizeMax;

    /**
     * 排序字段多选，顺序即优先级。取值：createTime / modifyTime / fileSize。
     * 空或未传时默认按 id 降序。各字段均降序，末尾追加 id DESC 保稳定。
     */
    private List<String> sortFields;
}
