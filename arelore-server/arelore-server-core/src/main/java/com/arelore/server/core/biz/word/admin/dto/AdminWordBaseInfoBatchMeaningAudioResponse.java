package com.arelore.server.core.biz.word.admin.dto;

import lombok.Data;

@Data
public class AdminWordBaseInfoBatchMeaningAudioResponse {
    private String source = "QWEN3_TTS";
    private String voice;
    /** 实际使用的客户端并发度 */
    private int parallelism;
    /** 扫描到的候选数 */
    private int scanned;
    /** 成功写入语音的词数 */
    private int filled;
    private int failed;
    private int skipped;
    private String note;
}
