package com.arelore.server.core.detection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 试卷编码 + 题目编码，用于批量查询题目。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionTypeCodePair {
    private String typeCode;
    private String questionCode;
}
