package com.arelore.server.user.controller;

import com.arelore.server.common.result.Result;
import com.arelore.server.user.dto.WechatQrCodeResponse;
import com.arelore.server.user.dto.WechatQrCodeStatusResponse;
import com.arelore.server.user.dto.WechatQuickLoginRequest;
import com.arelore.server.user.dto.MobileLoginRequest;
import com.arelore.server.user.dto.AuthLoginResponse;
import com.arelore.server.user.dto.AuthUserInfoResponse;
import com.arelore.server.user.dto.SceneIdRequest;
import com.arelore.server.user.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器（微信登录 + 移动端账号登录）。
 */
@Slf4j
@RestController
@RequestMapping("/api/user/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 获取微信扫码二维码
     */
    @PostMapping("/wechat/qrcode")
    public Result<WechatQrCodeResponse> getWechatQrCode() {
        return Result.success(authService.getWechatQrCode());
    }

    /**
     * 检查微信扫码状态
     */
    @PostMapping("/wechat/qrcode/check")
    public Result<WechatQrCodeStatusResponse> checkWechatQrCodeStatus(@RequestBody SceneIdRequest request) {
        String sceneId = request.getSceneId();
        
        if (sceneId == null || sceneId.isEmpty()) {
            return Result.error("场景 ID 不能为空");
        }
        
        try {
            WechatQrCodeStatusResponse response = authService.checkWechatQrCodeStatus(sceneId);
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 微信一键登录
     */
    @PostMapping("/wechat/quick")
    public Result<AuthLoginResponse> wechatQuickLogin(@RequestBody WechatQuickLoginRequest request) {
        try {
            AuthLoginResponse responseData = authService.wechatQuickLogin(request);
            return Result.success(responseData);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 移动端账号密码登录：
     * - 测试账号走 yml 中的测试密码校验
     * - 非测试账号走注册结果表密码校验
     */
    @PostMapping("/mobile/login")
    public Result<AuthLoginResponse> mobileLogin(@RequestBody MobileLoginRequest request) {
        try {
            AuthLoginResponse responseData = authService.mobileLogin(request);
            return Result.success(responseData);
        } catch (IllegalArgumentException e) {
            return Result.error(2003, e.getMessage());
        }
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        authService.logout(token);
        return Result.success(null);
    }

    /**
     * 获取当前用户信息
     */
    @PostMapping("/current")
    public Result<AuthUserInfoResponse> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        AuthUserInfoResponse user = authService.getCurrentUser(token);
        
        if (user == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        
        return Result.success(user);
    }
}
