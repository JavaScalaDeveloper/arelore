package com.arelore.server.core.biz.word.admin.dto;

import com.arelore.server.core.biz.word.admin.entity.AdminWordBook;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminWordBookResponse extends AdminWordBook {
    /** 封面图 URL，从 extInfo.cover 解析 */
    private String cover;
    /** 来源名称，从 extInfo.bookOrigin.originName 解析 */
    private String originName;
}
