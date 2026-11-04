package com.arelore.server.core.biz.word.admin.dto;

import lombok.Data;

import java.util.List;

/**
 * 从有道 + FreeDictionary 补释义/例句/多语种译词等；只扫 admin_word_base_info，不扫词条表。
 */
@Data
public class AdminWordBaseInfoEnrichDictRequest {
    /** 语种，可选 */
    private String languageCode;
    /** 词形前缀，可选 */
    private String word;
    /** 单标签（兼容旧入参） */
    private String tag;
    /** 多标签 AND，与列表筛选一致；可筛 no_freedictionary / no_meaning 等 */
    private List<String> tags;
    /** true=有图，false=无图；不传则不按是否有图限制 */
    private Boolean hasPicture;
    /** true=已确认，false=未确认 */
    private Boolean pictureConfirmed;
    /**
     * 最多处理多少个词；null/0 表示不限制。
     * 若设置则上限 50000。
     */
    private Integer limit;
}
