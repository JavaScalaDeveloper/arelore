package com.arelore.server.core.biz.word.admin;

import com.arelore.server.core.service.BaseService;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoAdoptPicturesRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoBatchI18nMeaningAudioRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoBatchI18nMeaningAudioResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoBatchMeaningAudioRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoBatchMeaningAudioResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoBatchSearchRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoBatchSearchResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoEnrichDictRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoPictureSearchResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoSyncResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoYoudaoTestResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordPictureContentResponse;

public interface AdminWordBaseInfoService extends BaseService<AdminWordBaseInfoRequest, AdminWordBaseInfoResponse> {
    /**
     * 仅从 admin_word_entry 补齐尚未入库的词形到 admin_word_base_info；
     * 不调有道 / FreeDictionary。
     */
    AdminWordBaseInfoSyncResponse syncFromEntries();

    /**
     * 仅扫 admin_word_base_info：有道补释义/音标/例句等，FreeDictionary 补多语种译词；
     * 带空格词跳过；不改已有配图。可按列表筛选缩小范围。
     */
    AdminWordBaseInfoSyncResponse enrichFromDictionaries(AdminWordBaseInfoEnrichDictRequest request);

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

    /**
     * 按 wordTranslations 批量生成多语种译词语音（Edge TTS 按语种换 Neural 音色），
     * 写入 common_binary_file，回写 ext_info.i18nMeaningAudios。
     */
    AdminWordBaseInfoBatchI18nMeaningAudioResponse batchGenerateI18nMeaningAudio(
        AdminWordBaseInfoBatchI18nMeaningAudioRequest request
    );

    /** 人工采纳配图并锁定，之后自动同步不会覆盖 pictures */
    AdminWordBaseInfoResponse adoptPictures(AdminWordBaseInfoAdoptPicturesRequest request);

    /** 按 fileId 读取已落库配图/语音，列表接口不带二进制 */
    AdminWordPictureContentResponse pictureContent(Long fileId);
}
