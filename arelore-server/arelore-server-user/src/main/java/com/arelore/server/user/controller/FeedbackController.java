package com.arelore.server.user.controller;

import com.arelore.server.core.biz.common.CommonFeedbackTicketService;
import com.arelore.server.core.biz.common.dto.CommonFeedbackTicketRequest;
import com.arelore.server.core.biz.common.dto.CommonFeedbackTicketResponse;
import com.arelore.server.core.biz.common.dto.FeedbackFileContentRequest;
import com.arelore.server.core.biz.common.dto.FeedbackFileContentResponse;
import com.arelore.server.core.biz.common.dto.FeedbackFileUploadRequest;
import com.arelore.server.core.biz.common.dto.FeedbackFileUploadResponse;
import com.arelore.server.core.biz.user.AuthService;
import com.arelore.server.core.biz.user.dto.AuthUserInfoResponse;
import com.arelore.server.core.common.dto.PageResult;
import com.arelore.server.core.common.exception.BusinessException;
import com.arelore.server.core.common.result.Result;
import com.arelore.server.core.common.result.ResultCode;
import com.arelore.server.core.registration.entity.UserRegistrationResult;
import com.arelore.server.core.registration.enums.AccountTypeEnum;
import com.arelore.server.core.registration.mapper.UserRegistrationResultMapper;
import com.arelore.server.core.registration.support.ClientIpUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * C 端意见反馈（客服工单）。
 */
@RestController
@RequestMapping("/api/user/feedback")
public class FeedbackController {
    private final CommonFeedbackTicketService ticketService;
    private final AuthService authService;
    private final UserRegistrationResultMapper registrationResultMapper;

    public FeedbackController(
        CommonFeedbackTicketService ticketService,
        AuthService authService,
        UserRegistrationResultMapper registrationResultMapper
    ) {
        this.ticketService = ticketService;
        this.authService = authService;
        this.registrationResultMapper = registrationResultMapper;
    }

    @PostMapping("/ticket/create")
    public Result<CommonFeedbackTicketResponse> create(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody CommonFeedbackTicketRequest request,
        HttpServletRequest httpServletRequest
    ) {
        try {
            BigDecimal userId = requireUserId(token);
            if (request == null) {
                request = new CommonFeedbackTicketRequest();
            }
            request.setClientIp(ClientIpUtils.getClientIp(httpServletRequest));
            return Result.success(ticketService.createTicket(userId, request));
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            return toAuthError(e);
        }
    }

    @PostMapping("/ticket/page")
    public Result<PageResult<CommonFeedbackTicketResponse>> page(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody(required = false) CommonFeedbackTicketRequest request
    ) {
        try {
            BigDecimal userId = requireUserId(token);
            CommonFeedbackTicketRequest req = request == null ? new CommonFeedbackTicketRequest() : request;
            req.setUserId(userId);
            return Result.success(ticketService.pageQuery(req));
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            return toAuthError(e);
        }
    }

    @PostMapping("/ticket/detail")
    public Result<CommonFeedbackTicketResponse> detail(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody CommonFeedbackTicketRequest request
    ) {
        try {
            BigDecimal userId = requireUserId(token);
            Long id = request == null ? null : request.getId();
            return Result.success(ticketService.detail(id, userId));
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            return toAuthError(e);
        }
    }

    @PostMapping("/ticket/reply")
    public Result<CommonFeedbackTicketResponse> reply(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody CommonFeedbackTicketRequest request,
        HttpServletRequest httpServletRequest
    ) {
        try {
            BigDecimal userId = requireUserId(token);
            if (request == null) {
                request = new CommonFeedbackTicketRequest();
            }
            request.setClientIp(ClientIpUtils.getClientIp(httpServletRequest));
            return Result.success(ticketService.reply(userId, CommonFeedbackTicketService.SENDER_USER, request));
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            return toAuthError(e);
        }
    }

    @PostMapping("/ticket/close")
    public Result<CommonFeedbackTicketResponse> close(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody CommonFeedbackTicketRequest request
    ) {
        try {
            BigDecimal userId = requireUserId(token);
            Long id = request == null ? null : request.getId();
            return Result.success(ticketService.close(id, userId, null));
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            return toAuthError(e);
        }
    }

    @PostMapping("/file/upload")
    public Result<FeedbackFileUploadResponse> upload(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody FeedbackFileUploadRequest request
    ) {
        try {
            requireUserId(token);
            return Result.success(ticketService.uploadFile(request));
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            return toAuthError(e);
        }
    }

    @PostMapping("/file/content")
    public Result<FeedbackFileContentResponse> fileContent(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody FeedbackFileContentRequest request
    ) {
        try {
            requireUserId(token);
            return Result.success(ticketService.fileContent(request));
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            return toAuthError(e);
        }
    }

    private <T> Result<T> toAuthError(IllegalArgumentException e) {
        if (e.getMessage() != null && e.getMessage().contains("未登录")) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), e.getMessage());
        }
        return Result.error(e.getMessage());
    }

    private BigDecimal requireUserId(String token) {
        AuthUserInfoResponse user = authService.getCurrentUser(token);
        if (user == null || !StringUtils.hasText(user.getId())) {
            throw new IllegalArgumentException("未登录或登录已过期");
        }
        String account = user.getId().trim();
        try {
            return new BigDecimal(account);
        } catch (NumberFormatException ignored) {
            // 微信 openid → registration.user_id
        }
        LambdaQueryWrapper<UserRegistrationResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRegistrationResult::getAccountType, AccountTypeEnum.WECHAT.code())
            .eq(UserRegistrationResult::getAccount, account)
            .orderByDesc(UserRegistrationResult::getId)
            .last("limit 1");
        UserRegistrationResult result = registrationResultMapper.selectOne(wrapper);
        if (result == null || result.getUserId() == null) {
            throw new IllegalArgumentException("未找到用户ID，请重新登录");
        }
        return result.getUserId();
    }
}
