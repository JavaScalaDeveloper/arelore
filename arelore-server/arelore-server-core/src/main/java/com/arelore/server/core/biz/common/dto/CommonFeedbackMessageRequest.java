package com.arelore.server.core.biz.common.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommonFeedbackMessageRequest extends CommonFeedbackMessageResponse {
    private Integer pageNum = 1;
    private Integer pageSize = 50;
}
