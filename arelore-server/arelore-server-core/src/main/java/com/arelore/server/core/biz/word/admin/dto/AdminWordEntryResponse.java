package com.arelore.server.core.biz.word.admin.dto;

import com.arelore.server.core.biz.word.admin.entity.AdminWordEntry;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminWordEntryResponse extends AdminWordEntry {
    /** 单词本中文名（列表/详情展示用，非表字段） */
    private String bookName;
}
