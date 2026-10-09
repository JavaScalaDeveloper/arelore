package com.arelore.server.core.biz.word.admin;

import com.arelore.server.core.service.BaseService;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoAdoptPicturesRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoPictureSearchResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoSyncResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoYoudaoTestResponse;

public interface AdminWordBaseInfoService extends BaseService<AdminWordBaseInfoRequest, AdminWordBaseInfoResponse> {
    /**
     * 分页扫描 admin_word_entry 全部词形，拉取外源后新增/更新 admin_word_base_info。
     */
    AdminWordBaseInfoSyncResponse syncFromEntries();

    /** 管理端调试：实时请求有道 jsonapi，不落库 */
    AdminWordBaseInfoYoudaoTestResponse testYoudao(String word);

    /**
     * 图库搜图（优先 Unsplash，其次 Pexels），至少 4 张候选；结果仅返回给前端，不落库。
     * 未采纳的候选下次搜图不会保留。
     */
    AdminWordBaseInfoPictureSearchResponse searchStockPictures(Long id);

    /** 人工采纳配图并锁定，之后自动同步不会覆盖 pictures */
    AdminWordBaseInfoResponse adoptPictures(AdminWordBaseInfoAdoptPicturesRequest request);
}
