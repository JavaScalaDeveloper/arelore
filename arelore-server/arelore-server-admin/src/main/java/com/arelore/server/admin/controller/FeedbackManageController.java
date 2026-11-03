package com.arelore.server.admin.controller;

import com.arelore.server.core.biz.common.CommonFeedbackTicketService;
import com.arelore.server.core.biz.common.dto.CommonFeedbackTicketRequest;
import com.arelore.server.core.biz.common.dto.CommonFeedbackTicketResponse;
import com.arelore.server.core.biz.common.dto.FeedbackFileContentRequest;
import com.arelore.server.core.biz.common.dto.FeedbackFileContentResponse;
import com.arelore.server.core.biz.common.dto.FeedbackFileUploadRequest;
import com.arelore.server.core.biz.common.dto.FeedbackFileUploadResponse;
import com.arelore.server.core.common.dto.PageResult;
import com.arelore.server.core.common.exception.BusinessException;
import com.arelore.server.core.common.result.Result;
import com.arelore.server.core.registration.support.ClientIpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 管理端意见反馈工单。
 */
@RestController
@RequestMapping("/api/admin/common/feedback")
public class FeedbackManageController {
    /** 管理端尚未落地真实操作员 ID，回复时暂记为 1 */
    private static final BigDecimal MOCK_ADMIN_USER_ID = BigDecimal.ONE;

    private final CommonFeedbackTicketService ticketService;

    public FeedbackManageController(CommonFeedbackTicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/ticket/page")
    public Result<PageResult<CommonFeedbackTicketResponse>> page(
        @RequestBody(required = false) CommonFeedbackTicketRequest request
    ) {
        try {
            return Result.success(ticketService.pageQuery(request == null ? new CommonFeedbackTicketRequest() : request));
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @PostMapping("/ticket/detail")
    public Result<CommonFeedbackTicketResponse> detail(@RequestBody CommonFeedbackTicketRequest request) {
        try {
            Long id = request == null ? null : request.getId();
            return Result.success(ticketService.detail(id, null));
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @PostMapping("/ticket/reply")
    public Result<CommonFeedbackTicketResponse> reply(
        @RequestBody CommonFeedbackTicketRequest request,
        HttpServletRequest httpServletRequest
    ) {
        try {
            if (request == null) {
                request = new CommonFeedbackTicketRequest();
            }
            request.setClientIp(ClientIpUtils.getClientIp(httpServletRequest));
            BigDecimal adminId = request.getAdminUserId() != null ? request.getAdminUserId() : MOCK_ADMIN_USER_ID;
            return Result.success(ticketService.reply(adminId, CommonFeedbackTicketService.SENDER_ADMIN, request));
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @PostMapping("/ticket/close")
    public Result<CommonFeedbackTicketResponse> close(@RequestBody CommonFeedbackTicketRequest request) {
        try {
            Long id = request == null ? null : request.getId();
            return Result.success(ticketService.close(id, null, MOCK_ADMIN_USER_ID));
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @PostMapping("/ticket/update-status")
    public Result<CommonFeedbackTicketResponse> updateStatus(@RequestBody CommonFeedbackTicketRequest request) {
        try {
            if (request == null || request.getId() == null) {
                return Result.error("工单ID不能为空");
            }
            return Result.success(ticketService.updateStatus(request.getId(), request.getStatus(), MOCK_ADMIN_USER_ID));
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @PostMapping("/file/upload")
    public Result<FeedbackFileUploadResponse> upload(@RequestBody FeedbackFileUploadRequest request) {
        try {
            return Result.success(ticketService.uploadFile(request));
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @PostMapping("/file/content")
    public Result<FeedbackFileContentResponse> fileContent(@RequestBody FeedbackFileContentRequest request) {
        try {
            return Result.success(ticketService.fileContent(request));
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
