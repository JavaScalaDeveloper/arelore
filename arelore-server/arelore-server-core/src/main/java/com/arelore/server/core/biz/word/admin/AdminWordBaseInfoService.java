package com.arelore.server.core.biz.word.admin;

import com.arelore.server.core.service.BaseService;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoAdoptPicturesRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoBatchSearchRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoBatchSearchResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoPictureSearchResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoSyncResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoYoudaoTestResponse;

public interface AdminWordBaseInfoService extends BaseService<AdminWordBaseInfoRequest, AdminWordBaseInfoResponse> {
    /**
     * 分页扫描 admin_word_entry 全部词形，仅同步语种+词形到 admin_word_base_info（不查有道、不配图）。
     */
    AdminWordBaseInfoSyncResponse syncFromEntries();

    /** 管理端调试：实时请求有道 jsonapi，不落库 */
    AdminWordBaseInfoYoudaoTestResponse testYoudao(String word);

    /**
     * 单词 Pixabay 搜图，至少 4 张候选；结果仅返回给前端，不落库。
     */
    AdminWordBaseInfoPictureSearchResponse searchStockPictures(Long id);

    /**
     * 批量搜图：分页扫无图词。
     * PIXABAY → 未确认配图；YOUDAO → 有道配图并直接确认可用。
     */
    AdminWordBaseInfoBatchSearchResponse batchSearchPictures(AdminWordBaseInfoBatchSearchRequest request);

    /** 人工采纳配图并锁定，之后自动同步不会覆盖 pictures */
    AdminWordBaseInfoResponse adoptPictures(AdminWordBaseInfoAdoptPicturesRequest request);
}
