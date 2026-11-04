package com.arelore.server.core.biz.word.admin.dto;

import lombok.Data;

import java.util.List;

/**
 * 按 wordTranslations 批量生成多语种译词语音（Edge TTS，按语种切换 Neural 音色）。
 * 写入 common_binary_file，回写 ext_info.i18nMeaningAudios。
 */
@Data
public class AdminWordBaseInfoBatchI18nMeaningAudioRequest {
    private String languageCode;
    private String word;
    private String tag;
    private List<String> tags;
    private Boolean hasPicture;
    private Boolean pictureConfirmed;
    /** 最多处理词数；null/0=不限制，上限 50000 */
    private Integer limit;
    /** true=即使已有语音也重生成 */
    private Boolean force;
    /** 只合成这些语种；空=词条已有译词里、且目录有音色的语种（每词上限 maxLangsPerWord） */
    private List<String> langs;
    /** 每个词最多合成几个语种，默认 12，上限 20 */
    private Integer maxLangsPerWord;
    private Integer parallelism;
}
