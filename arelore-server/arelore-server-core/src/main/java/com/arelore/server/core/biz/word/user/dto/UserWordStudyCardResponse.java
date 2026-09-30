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
    /** 配图 URL（ext_info.picture） */
    private String picture;
    /** 记忆法（ext_info.remMethod.val） */
    private String mnemonic;
    /** 英文例句（提示用，取首条） */
    private String exampleEn;
    /** 中文例句（取首条） */
    private String exampleCn;
    /** 完整例句列表 */
    private List<UserWordStudyExampleResponse> examples = new ArrayList<>();
}
