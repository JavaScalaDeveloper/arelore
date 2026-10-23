package com.arelore.server.core.biz.word.user.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserWordStudyCardResponse {
    /** learn | review */
    private String mode;
    private String wordCode;
    private String word;
    private String phonetic;
    private String meaning;
    /** 首张配图（来自 admin_word_base_info，便于单图展示） */
    private String picture;
    /** 全部配图 URL（来自 admin_word_base_info.ext_info.pictures） */
    private List<String> pictures = new ArrayList<>();
    /** 已落入 common_binary_file 的配图，与 pictures 按写入顺序对应；学习端用它读本地图 */
    private List<Long> pictureFileIds = new ArrayList<>();
    /** 已落入 common_binary_file 的释义整段语音（base_info.ext_info.meaningAudio.fileId） */
    private Long meaningAudioFileId;
    /** 记忆法（ext_info.remMethod.val） */
    private String mnemonic;
    /** 英文例句（提示用，取首条） */
    private String exampleEn;
    /** 中文例句（取首条） */
    private String exampleCn;
    /** 完整例句列表（词书 entry 优先，否则 base_info 有道补全） */
    private List<UserWordStudyExampleResponse> examples = new ArrayList<>();
    /** 词组搭配 */
    private List<UserWordStudyPhraseResponse> phrases = new ArrayList<>();
    /** 近义分组 */
    private List<UserWordStudySynonymGroupResponse> synonyms = new ArrayList<>();
    /** 派生/相关词分组 */
    private List<UserWordStudyRelatedGroupResponse> relatedWords = new ArrayList<>();
    /** 词源摘要（有道） */
    private String etym;
}
