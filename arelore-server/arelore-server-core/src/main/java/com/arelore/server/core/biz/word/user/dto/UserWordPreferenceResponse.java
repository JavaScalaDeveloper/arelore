package com.arelore.server.core.biz.word.user.dto;

import com.arelore.server.core.biz.word.user.entity.UserWordPreference;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserWordPreferenceResponse extends UserWordPreference {
    /** 用户业务 ID 字符串（对应 userId），避免前端精度丢失 */
    private String idStr;

    /**
     * 背词模式（从 ext_info.studyMode 解析）：
     * see_meaning=看词识意，listen_spell=听词（摸鱼），dictation=听写，see_pick_image=看词选图
     */
    private String studyMode;

    /**
     * 拼读语速 0~10，数字越大越快（听词模式；存于 ext_info.spellSpeed）
     */
    private Integer spellSpeed;

    /**
     * 单词连读次数 1~3（听词模式只重复读单词，不重复拼读；存于 ext_info.wordRepeatCount）
     */
    private Integer wordRepeatCount;
}
