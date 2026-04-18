package com.arelore.server.user.controller;

import com.arelore.server.common.result.Result;
import com.arelore.server.common.exception.BusinessException;
import com.arelore.server.common.result.ResultCode;
import com.arelore.server.core.registration.dto.MobileRegisterApplyRequest;
import com.arelore.server.core.registration.dto.MobileRegisterVerifyRequest;
import com.arelore.server.core.registration.service.UserRegistrationService;
import com.arelore.server.core.registration.support.ClientIpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
            return Result.error(e.getCode(), sanitizeMessage(e.getMessage()));
        } catch (Exception e) {
            return Result.error(ResultCode.USER_REGISTER_APPLY_FAILED, "注册申请失败，请稍后再试");
        }
    }

    @PostMapping("/mobile/verify")
    public Result<Map<String, Object>> verify(@RequestBody MobileRegisterVerifyRequest request, HttpServletRequest httpServletRequest) {
        String ip = ClientIpUtils.getClientIp(httpServletRequest);
        String userId = registrationService.verifyMobileRegister(request, ip);
        return Result.success(Map.of("userId", userId));
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

