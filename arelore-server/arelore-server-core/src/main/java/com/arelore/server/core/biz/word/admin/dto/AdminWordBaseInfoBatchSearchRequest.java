package com.arelore.server.core.biz.word.admin.dto;

import lombok.Data;

/**
 * 批量搜图：默认分页扫完全部无图词。
 * source=PIXABAY 写入未确认配图；source=YOUDAO 写入有道配图并直接确认可用。
 */
@Data
public class AdminWordBaseInfoBatchSearchRequest {
    /** 语种，可选 */
    private String languageCode;
    /** 标签，默认 no_picture */
    private String tag = "no_picture";
    /** 图源：PIXABAY（默认）或 YOUDAO */
    private String source = "PIXABAY";
    /**
     * 最多处理多少个词；null/0 表示不限制（扫完全部无图词）。
     * 若设置则上限 50000。
     */
    private Integer limit;
    /** Pixabay 每个词写入张数，默认 4，上限 12；有道按接口返回全部可用图 */
    private Integer picturesPerWord = 4;
}
