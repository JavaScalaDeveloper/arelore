package com.arelore.server.core.biz.word.admin.dto;

import lombok.Data;

@Data
public class AdminWordBaseInfoSyncResponse {
    /** 扫描到的词条行数 */
    private int scanned;
    /** 去重后待同步词数 */
    private int distinct;
    private int inserted;
    private int updated;
    private int failed;
    private int skipped;
    /** 有道补释义扫描数（无释义词，不含带空格词） */
    private int meaningScanned;
    /** 有道补释义成功写入数 */
    private int meaningFilled;
    /** 有道补释义失败/空结果数 */
    private int meaningFailed;
    /** 跳过（含带空格、已标记 youdao_empty 等） */
    private int meaningSkipped;
    private String note;
}
