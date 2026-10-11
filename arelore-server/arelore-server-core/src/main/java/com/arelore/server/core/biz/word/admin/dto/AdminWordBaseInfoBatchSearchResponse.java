package com.arelore.server.core.biz.word.admin.dto;

import lombok.Data;

@Data
public class AdminWordBaseInfoBatchSearchResponse {
    private String source = "PIXABAY";
    /** 扫描到的无图候选数 */
    private int scanned;
    /** 成功写入配图的词数 */
    private int filled;
    private int failed;
    private int skipped;
    private String note;
}
