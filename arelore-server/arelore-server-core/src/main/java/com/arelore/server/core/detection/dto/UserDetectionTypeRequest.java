package com.arelore.server.core.detection.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserDetectionTypeRequest extends UserDetectionTypeResponse {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}

