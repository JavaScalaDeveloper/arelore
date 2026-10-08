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
    /** 记忆法（ext_info.remMethod.val） */
    private String mnemonic;
    /** 英文例句（提示用，取首条） */
    private String exampleEn;
    /** 中文例句（取首条） */
    private String exampleCn;
    /** 完整例句列表 */
    private List<UserWordStudyExampleResponse> examples = new ArrayList<>();
}
