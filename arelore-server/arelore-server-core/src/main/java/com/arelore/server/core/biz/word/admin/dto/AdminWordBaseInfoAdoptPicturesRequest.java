package com.arelore.server.core.biz.word.admin.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminWordBaseInfoAdoptPicturesRequest {
    private Long id;
    /** 勾选采纳的图片 URL（来自本次搜图候选） */
    private List<String> pictureUrls = new ArrayList<>();
}
