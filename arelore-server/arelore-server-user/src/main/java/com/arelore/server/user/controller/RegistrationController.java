package com.arelore.server.user.controller;

import com.arelore.server.core.common.result.Result;
import com.arelore.server.core.common.exception.BusinessException;
import com.arelore.server.core.common.result.ResultCode;
import com.arelore.server.core.registration.dto.MobileRegisterApplyRequest;
import com.arelore.server.core.registration.dto.MobileRegisterVerifyRequest;
import com.arelore.server.core.registration.dto.MobileResetPasswordApplyRequest;
import com.arelore.server.core.registration.dto.MobileResetPasswordConfirmRequest;
import com.arelore.server.core.biz.user.UserRegistrationService;
import com.arelore.server.core.registration.support.ClientIpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user/register")
public class RegistrationController {
    private final UserRegistrationService registrationService;

    public RegistrationController(UserRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/mobile/apply")
    public Result<String> apply(@RequestBody MobileRegisterApplyRequest request, HttpServletRequest httpServletRequest) {
        String ip = ClientIpUtils.getClientIp(httpServletRequest);
        try {
            registrationService.applyMobileRegister(request, ip);
            return Result.success("验证码已发送");
        } catch (BusinessException e) {
            log.warn("mobile register apply failed, mobile={}, ip={}, message={}",
                request == null ? "" : request.getMobile(), ip, e.getMessage(), e);
            return Result.error(e.getCode(), sanitizeMessage(e.getMessage()));
        } catch (Exception e) {
            log.error("mobile register apply system error, mobile={}, ip={}",
                request == null ? "" : request.getMobile(), ip, e);
            return Result.error(ResultCode.USER_REGISTER_APPLY_FAILED, "注册申请失败，请稍后再试");
        }
    }

    @PostMapping("/mobile/verify")
    public Result<Map<String, Object>> verify(@RequestBody MobileRegisterVerifyRequest request, HttpServletRequest httpServletRequest) {
        String ip = ClientIpUtils.getClientIp(httpServletRequest);
        String userId = registrationService.verifyMobileRegister(request, ip);
        return Result.success(Map.of("userId", userId));
    }

    @PostMapping("/mobile/password-reset/apply")
    public Result<String> applyResetPassword(
        @RequestBody MobileResetPasswordApplyRequest request,
        HttpServletRequest httpServletRequest
    ) {
        String ip = ClientIpUtils.getClientIp(httpServletRequest);
        try {
            registrationService.applyMobileResetPassword(request, ip);
            return Result.success("验证码已发送");
        } catch (BusinessException e) {
            log.warn("mobile reset password apply failed, mobile={}, ip={}, message={}",
                request == null ? "" : request.getMobile(), ip, e.getMessage(), e);
            return Result.error(e.getCode(), sanitizeMessage(e.getMessage()));
        } catch (Exception e) {
            log.error("mobile reset password apply system error, mobile={}, ip={}",
                request == null ? "" : request.getMobile(), ip, e);
            return Result.error(ResultCode.USER_REGISTER_APPLY_FAILED, "找回密码申请失败，请稍后再试");
        }
    }

    @PostMapping("/mobile/password-reset/confirm")
    public Result<String> confirmResetPassword(
        @RequestBody MobileResetPasswordConfirmRequest request,
        HttpServletRequest httpServletRequest
    ) {
        String ip = ClientIpUtils.getClientIp(httpServletRequest);
        try {
            registrationService.confirmMobileResetPassword(request, ip);
            return Result.success("密码重置成功");
        } catch (BusinessException e) {
            log.warn("mobile reset password confirm failed, mobile={}, ip={}, message={}",
                request == null ? "" : request.getMobile(), ip, e.getMessage(), e);
            return Result.error(e.getCode(), sanitizeMessage(e.getMessage()));
        } catch (Exception e) {
            log.error("mobile reset password confirm system error, mobile={}, ip={}",
                request == null ? "" : request.getMobile(), ip, e);
            return Result.error(ResultCode.USER_REGISTER_APPLY_FAILED, "密码重置失败，请稍后再试");
        }
    }

    private String sanitizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "操作失败，请稍后再试";
        }
        String lower = message.toLowerCase();
        if (lower.contains("sql")
            || lower.contains("jdbc")
            || lower.contains("table")
            || lower.contains("column")
            || lower.contains("constraint")
            || lower.contains("exception")) {
            return "操作失败，请稍后再试";
        }
        return message;
    }
}

