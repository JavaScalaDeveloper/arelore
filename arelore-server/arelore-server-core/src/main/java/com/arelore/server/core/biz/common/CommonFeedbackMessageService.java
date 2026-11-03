package com.arelore.server.core.biz.common;

import com.arelore.server.core.biz.common.dto.CommonFeedbackMessageRequest;
import com.arelore.server.core.biz.common.dto.CommonFeedbackMessageResponse;
import com.arelore.server.core.service.BaseService;

import java.util.List;

public interface CommonFeedbackMessageService
    extends BaseService<CommonFeedbackMessageRequest, CommonFeedbackMessageResponse> {

    List<CommonFeedbackMessageResponse> listByTicketId(Long ticketId);
}
