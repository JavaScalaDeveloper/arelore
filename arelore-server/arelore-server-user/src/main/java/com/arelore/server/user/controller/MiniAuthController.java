package com.arelore.server.user.controller;

import com.arelore.server.core.common.result.Result;
import com.arelore.server.core.registration.support.ClientIpUtils;
import com.arelore.server.core.biz.user.dto.AuthLoginResponse;
import com.arelore.server.core.biz.user.dto.MiniManualLoginRequest;
import com.arelore.server.user.service.MiniAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小程序认证接口。
 */
@Slf4j
@RestController
@RequestMapping("/api/user/mini/auth")
public class MiniAuthController {
    private final MiniAuthService miniAuthService;

    public MiniAuthController(MiniAuthService miniAuthService) {
        this.miniAuthService = miniAuthService;
    }

    /**
     * 手册登录（手机号 + 密码摘要）。
     * 登录时会将流水写入 user_registration_application，并创建/校验 user_registration_result。
     */
    @PostMapping("/manual/login")
    public Result<AuthLoginResponse> manualLogin(@RequestBody MiniManualLoginRequest request, HttpServletRequest httpServletRequest) {
        String ip = ClientIpUtils.getClientIp(httpServletRequest);
        AuthLoginResponse resp = miniAuthService.manualLogin(request, ip);
        return Result.success(resp);
    }
}

