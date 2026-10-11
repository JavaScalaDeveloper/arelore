package com.arelore.server.core.biz.word.admin.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminWordBaseInfoRequest extends AdminWordBaseInfoResponse {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    /** 单标签（兼容旧入参） */
    private String tag;
    /** 多标签 AND：如 ["no_picture","source_pixabay"] */
    private List<String> tags = new ArrayList<>();
    /** true=有图，false=无图 */
    private Boolean hasPicture;
    /** true=已人工确认 */
    private Boolean pictureConfirmed;
    /** YOUDAO / PIXABAY / NONE */
    private String pictureSource;
}
