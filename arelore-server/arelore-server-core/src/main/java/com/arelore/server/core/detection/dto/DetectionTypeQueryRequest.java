package com.arelore.server.core.detection.dto;

import lombok.Data;

@Data
public class DetectionTypeQueryRequest {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String typeCode;
    private String typeName;
}
