package com.arelore.server.core.biz.word.admin.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量搜图：按当前筛选分页扫描。
 * source=PIXABAY 写入未确认配图；source=YOUDAO 写入有道配图并直接确认可用。
 */
@Data
public class AdminWordBaseInfoBatchSearchRequest {
    /** 语种，可选 */
    private String languageCode;
    /** 词形前缀，可选 */
    private String word;
    /** 单标签（兼容旧入参）；不传则不按标签限制 */
    private String tag;
    /** 多标签 AND，与列表筛选一致 */
    private List<String> tags;
    /** 图源：PIXABAY（默认）或 YOUDAO */
    private String source = "PIXABAY";
    /**
     * 最多处理多少个词；null/0 表示不限制（扫完全部无图词）。
     * 若设置则上限 50000。
     */
    private Integer limit;
    /** Pixabay 每个词写入张数，默认 4，上限 12；有道按接口返回全部可用图 */
    private Integer picturesPerWord = 4;
    /** true=有图，false=无图；不传则不按是否有图限制 */
    private Boolean hasPicture;
    /** true=已确认，false=未确认 */
    private Boolean pictureConfirmed;
}
