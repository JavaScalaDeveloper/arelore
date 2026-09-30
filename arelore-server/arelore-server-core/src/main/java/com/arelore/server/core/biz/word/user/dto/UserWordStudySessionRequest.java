package com.arelore.server.core.biz.word.user.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserWordStudySessionRequest {
    private BigDecimal userId;
    private String bookCode;
}
