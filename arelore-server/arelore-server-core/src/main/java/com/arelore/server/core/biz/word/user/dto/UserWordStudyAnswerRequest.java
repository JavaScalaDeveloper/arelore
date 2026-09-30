package com.arelore.server.core.biz.word.user.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserWordStudyAnswerRequest {
    private BigDecimal userId;
    private String bookCode;
    private String wordCode;
    /** learn | review */
    private String mode;
    /** true=认识 false=不认识 */
    private Boolean rememberFlag;
}
