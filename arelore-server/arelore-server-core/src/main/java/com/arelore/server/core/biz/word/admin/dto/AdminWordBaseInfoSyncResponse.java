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
}
