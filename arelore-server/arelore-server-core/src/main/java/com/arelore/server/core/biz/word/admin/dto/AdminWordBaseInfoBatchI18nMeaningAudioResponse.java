package com.arelore.server.core.biz.word.admin.dto;

import lombok.Data;

@Data
public class AdminWordBaseInfoBatchI18nMeaningAudioResponse {
    private String source;
    private int scanned;
    private int filled;
    private int failed;
    private int skipped;
    /** 实际合成的「词×语种」条数 */
    private int audioFilled;
    private int parallelism;
    private String note;
}
