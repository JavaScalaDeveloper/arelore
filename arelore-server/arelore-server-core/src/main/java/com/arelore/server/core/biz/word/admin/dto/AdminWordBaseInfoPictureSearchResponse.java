package com.arelore.server.core.biz.word.admin.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class AdminWordBaseInfoPictureSearchResponse {
    private Long id;
    private String word;
    private String source;
    /** 仅本次接口返回，不落库；未采纳下次搜图不会保留 */
    private List<Map<String, String>> candidates = new ArrayList<>();
    private String note;
}
