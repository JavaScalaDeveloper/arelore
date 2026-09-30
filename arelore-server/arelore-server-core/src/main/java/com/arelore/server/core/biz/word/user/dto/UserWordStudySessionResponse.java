package com.arelore.server.core.biz.word.user.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserWordStudySessionResponse {
    private String bookCode;
    private String bookName;
    /** 有道发音 type：1-英音 2-美音，默认 2 */
    private Integer voiceType = 2;
    /** 今日新学已完成数（可断点续学） */
    private Integer learnDone = 0;
    private Integer learnTotal = 0;
    /** 今日复习已完成数（可断点续学） */
    private Integer reviewDone = 0;
    private Integer reviewTotal = 0;
    private List<UserWordStudyCardResponse> cards = new ArrayList<>();
}
