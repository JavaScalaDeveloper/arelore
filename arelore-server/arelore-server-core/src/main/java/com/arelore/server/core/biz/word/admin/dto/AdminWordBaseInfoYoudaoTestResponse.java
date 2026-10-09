package com.arelore.server.core.biz.word.admin.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class AdminWordBaseInfoYoudaoTestResponse {
    private String word;
    private boolean success;
    private String error;
    private Integer httpStatus;
    private Long elapsedMs;
    /** meta.dicts，便于判断有无 pic_dict */
    private List<String> dicts = new ArrayList<>();
    private boolean hasPicDict;
    private int pictureCount;
    private List<String> pictureUrls = new ArrayList<>();
    /** 精简后的 ext（与入库结构一致） */
    private Map<String, Object> slimExt;
    /** 说明：抽象词有道常无配图 */
    private String note;
}
