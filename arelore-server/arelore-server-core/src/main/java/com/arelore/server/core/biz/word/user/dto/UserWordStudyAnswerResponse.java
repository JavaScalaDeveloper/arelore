package com.arelore.server.core.biz.word.user.dto;

import lombok.Data;

@Data
public class UserWordStudyAnswerResponse {
    private Integer learnTodo;
    private Integer learnDone;
    private Integer reviewTodo;
    private Integer reviewDone;
}
