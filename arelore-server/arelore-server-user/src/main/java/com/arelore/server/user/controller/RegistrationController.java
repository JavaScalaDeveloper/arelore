package com.arelore.server.user.controller;

import com.arelore.server.common.result.Result;
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
        registrationService.applyMobileRegister(request, ip);
        return Result.success("验证码已发送");
    }

    @PostMapping("/mobile/verify")
    public Result<Map<String, Object>> verify(@RequestBody MobileRegisterVerifyRequest request, HttpServletRequest httpServletRequest) {
        String ip = ClientIpUtils.getClientIp(httpServletRequest);
        String userId = registrationService.verifyMobileRegister(request, ip);
        return Result.success(Map.of("userId", userId));
    }
}

