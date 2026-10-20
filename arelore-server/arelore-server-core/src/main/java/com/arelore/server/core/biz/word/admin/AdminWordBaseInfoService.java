package com.arelore.server.core.biz.word.admin;

import com.arelore.server.core.service.BaseService;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoAdoptPicturesRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoBatchMeaningAudioRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoBatchMeaningAudioResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoBatchSearchRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoBatchSearchResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoPictureSearchResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoSyncResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoYoudaoTestResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordPictureContentResponse;

public interface AdminWordBaseInfoService extends BaseService<AdminWordBaseInfoRequest, AdminWordBaseInfoResponse> {
    /**
     * 分页扫描 admin_word_entry 全部词形写入 admin_word_base_info，
     * 再对无释义词查有道补释义/音标（带空格词跳过；不改已有配图）。
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

    /**
     * 批量用本地 Qwen3-TTS 生成释义整段语音，写入 common_binary_file，
     * 并回写 base_info.ext_info.meaningAudio。
     */
    AdminWordBaseInfoBatchMeaningAudioResponse batchGenerateMeaningAudio(AdminWordBaseInfoBatchMeaningAudioRequest request);

    /** 人工采纳配图并锁定，之后自动同步不会覆盖 pictures */
    AdminWordBaseInfoResponse adoptPictures(AdminWordBaseInfoAdoptPicturesRequest request);

    /** 按 fileId 读取已落库配图/语音，列表接口不带二进制 */
    AdminWordPictureContentResponse pictureContent(Long fileId);
}
