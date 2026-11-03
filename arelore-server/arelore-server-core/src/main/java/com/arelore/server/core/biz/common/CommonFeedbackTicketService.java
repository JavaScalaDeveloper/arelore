package com.arelore.server.core.biz.common;

import com.arelore.server.core.biz.common.dto.CommonFeedbackTicketRequest;
import com.arelore.server.core.biz.common.dto.CommonFeedbackTicketResponse;
import com.arelore.server.core.biz.common.dto.FeedbackFileContentRequest;
import com.arelore.server.core.biz.common.dto.FeedbackFileContentResponse;
import com.arelore.server.core.biz.common.dto.FeedbackFileUploadRequest;
import com.arelore.server.core.biz.common.dto.FeedbackFileUploadResponse;
import com.arelore.server.core.service.BaseService;

import java.math.BigDecimal;

public interface CommonFeedbackTicketService
    extends BaseService<CommonFeedbackTicketRequest, CommonFeedbackTicketResponse> {

    String SENDER_USER = "USER";
    String SENDER_ADMIN = "ADMIN";

    int STATUS_PENDING_ADMIN = 1;
    int STATUS_IN_PROGRESS = 2;
    int STATUS_PENDING_USER = 3;
    int STATUS_CLOSED = 4;

    /**
     * 用户新建工单（含首条消息）。每用户每天最多 6 单。
     */
    CommonFeedbackTicketResponse createTicket(BigDecimal userId, CommonFeedbackTicketRequest request);

    /**
     * 用户或客服回复（插入消息并 bump 工单 modify_time / status）。
     */
    CommonFeedbackTicketResponse reply(BigDecimal senderUserId, String senderType, CommonFeedbackTicketRequest request);

    /**
     * 工单详情（含消息列表）。userId 非空时校验归属。
     */
    CommonFeedbackTicketResponse detail(Long ticketId, BigDecimal userIdOrNull);

    /**
     * 关闭工单。userId 非空时校验归属。
     */
    CommonFeedbackTicketResponse close(Long ticketId, BigDecimal userIdOrNull, BigDecimal adminUserIdOrNull);

    /**
     * 客服更新状态（处理中等）。
     */
    CommonFeedbackTicketResponse updateStatus(Long ticketId, Integer status, BigDecimal adminUserId);

    FeedbackFileUploadResponse uploadFile(FeedbackFileUploadRequest request);

    FeedbackFileContentResponse fileContent(FeedbackFileContentRequest request);
}
