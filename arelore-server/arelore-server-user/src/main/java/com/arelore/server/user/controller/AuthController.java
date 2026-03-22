package com.arelore.server.user.controller;

import com.arelore.server.common.result.Result;
import com.arelore.server.user.dto.WechatQrCodeResponse;
import com.arelore.server.user.dto.WechatQrCodeStatusResponse;
import com.arelore.server.user.dto.WechatQuickLoginRequest;
import com.arelore.server.user.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 微信登录控制器
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
    public Result<WechatQrCodeStatusResponse> checkWechatQrCodeStatus(@RequestBody Map<String, String> request) {
        String sceneId = request.get("sceneId");
        
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
    public Result<Map<String, Object>> wechatQuickLogin(@RequestBody WechatQuickLoginRequest request) {
        try {
            Map<String, Object> responseData = authService.wechatQuickLogin(request);
            return Result.success(responseData);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
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
    public Result<Map<String, Object>> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> user = authService.getCurrentUser(token);
        
        if (user == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        
        return Result.success(user);
    }
}
