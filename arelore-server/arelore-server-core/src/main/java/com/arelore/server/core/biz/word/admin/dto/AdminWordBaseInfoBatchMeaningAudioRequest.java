package com.arelore.server.core.biz.word.admin.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量用本地 Qwen3-TTS 生成释义语音，写入 common_binary_file，并回写 base_info.ext_info.meaningAudio。
 */
@Data
public class AdminWordBaseInfoBatchMeaningAudioRequest {
    /** 语种，可选 */
    private String languageCode;
    /** 词形前缀，可选 */
    private String word;
    /** 单标签（兼容旧入参） */
    private String tag;
    /** 多标签 AND，与列表筛选一致 */
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
    /** true=即使已有语音也按当前释义重生成 */
    private Boolean force;
    /**
     * 客户端并发调用 TTS 的线程数；null 用服务端配置 arelore.tts.qwen.parallelism。
     * Qwen3-TTS 默认 TTS_MAX_CONCURRENT=1，多请求会在服务端排队，一般不致失败。
     * 建议 2～4；上限 8。
     */
    private Integer parallelism;
}
