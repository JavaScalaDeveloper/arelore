package com.arelore.server.core.biz.word.admin.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminWordBaseInfoAdoptPicturesRequest {
    private Long id;
    /** 勾选采纳的图片 URL（来自本次搜图候选） */
    private List<String> pictureUrls = new ArrayList<>();
    /** 图源：PIXABAY / UNSPLASH / PEXELS，缺省取当前激活图库 */
    private String pictureSource;
}
