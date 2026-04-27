package com.arelore.server.core.detection.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserDetectResultHistoryRequest extends UserDetectResultHistoryResponse {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}

