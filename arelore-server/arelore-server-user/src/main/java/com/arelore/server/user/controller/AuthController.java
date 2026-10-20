package com.arelore.server.user.controller;

import com.arelore.server.core.common.result.Result;
import com.arelore.server.core.biz.user.dto.WechatQrCodeResponse;
import com.arelore.server.core.biz.user.dto.WechatQrCodeStatusResponse;
import com.arelore.server.core.biz.user.dto.WechatQuickLoginRequest;
import com.arelore.server.core.biz.user.dto.MobileLoginRequest;
import com.arelore.server.core.biz.user.dto.EmailCodeApplyRequest;
import com.arelore.server.core.biz.user.dto.EmailCodeLoginRequest;
import com.arelore.server.core.biz.user.dto.EmailRegisterConfirmRequest;
import com.arelore.server.core.biz.user.dto.EmailPasswordLoginRequest;
import com.arelore.server.core.biz.user.dto.EmailResetPasswordConfirmRequest;
import com.arelore.server.core.biz.user.dto.AuthLoginResponse;
import com.arelore.server.core.biz.user.dto.AuthUserInfoResponse;
import com.arelore.server.core.biz.user.dto.SceneIdRequest;
import com.arelore.server.core.biz.user.AuthService;
import com.arelore.server.core.common.exception.BusinessException;
import com.arelore.server.core.registration.support.ClientIpUtils;
import jakarta.servlet.http.HttpServletRequest;
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
            log.warn("checkWechatQrCodeStatus failed, sceneId={}, message={}", sceneId, e.getMessage(), e);
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
            log.warn("wechatQuickLogin failed, message={}", e.getMessage(), e);
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
            log.warn("mobileLogin failed, username={}, message={}", request == null ? "" : request.getUsername(), e.getMessage(), e);
            return Result.error(2003, e.getMessage());
        }
    }

    /**
     * 申请邮箱登录验证码（个人 SMTP；未注册邮箱也可申请，登录时自动注册）。
     */
    @PostMapping("/email/code/apply")
    public Result<String> applyEmailLoginCode(
        @RequestBody EmailCodeApplyRequest request,
        HttpServletRequest httpServletRequest
    ) {
        String ip = ClientIpUtils.getClientIp(httpServletRequest);
        try {
            authService.applyEmailLoginCode(request, ip);
            return Result.success("验证码已发送，请查收邮件（含垃圾箱）");
        } catch (BusinessException e) {
            log.warn("applyEmailLoginCode failed, email={}, ip={}, message={}",
                request == null ? "" : request.getEmail(), ip, e.getMessage(), e);
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("applyEmailLoginCode system error, email={}, ip={}",
                request == null ? "" : request.getEmail(), ip, e);
            return Result.error("验证码发送失败，请稍后再试");
        }
    }

    /**
     * 邮箱验证码登录；账号不存在时自动注册 EMAIL 账号。
     */
    @PostMapping("/email/code/login")
    public Result<AuthLoginResponse> emailCodeLogin(
        @RequestBody EmailCodeLoginRequest request,
        HttpServletRequest httpServletRequest
    ) {
        String ip = ClientIpUtils.getClientIp(httpServletRequest);
        try {
            AuthLoginResponse responseData = authService.emailCodeLogin(request, ip);
            return Result.success(responseData);
        } catch (BusinessException e) {
            log.warn("emailCodeLogin failed, email={}, ip={}, message={}",
                request == null ? "" : request.getEmail(), ip, e.getMessage(), e);
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("emailCodeLogin system error, email={}, ip={}",
                request == null ? "" : request.getEmail(), ip, e);
            return Result.error("登录失败，请稍后再试");
        }
    }

    /**
     * 申请邮箱注册验证码。
     */
    @PostMapping("/email/register/apply")
    public Result<String> applyEmailRegisterCode(
        @RequestBody EmailCodeApplyRequest request,
        HttpServletRequest httpServletRequest
    ) {
        String ip = ClientIpUtils.getClientIp(httpServletRequest);
        try {
            authService.applyEmailRegisterCode(request, ip);
            return Result.success("验证码已发送，请查收邮件（含垃圾箱）");
        } catch (BusinessException e) {
            log.warn("applyEmailRegisterCode failed, email={}, ip={}, message={}",
                request == null ? "" : request.getEmail(), ip, e.getMessage(), e);
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("applyEmailRegisterCode system error, email={}, ip={}",
                request == null ? "" : request.getEmail(), ip, e);
            return Result.error("验证码发送失败，请稍后再试");
        }
    }

    /**
     * 邮箱注册确认（验证码 + 密码），成功后直接登录。
     */
    @PostMapping("/email/register/confirm")
    public Result<AuthLoginResponse> confirmEmailRegister(
        @RequestBody EmailRegisterConfirmRequest request,
        HttpServletRequest httpServletRequest
    ) {
        String ip = ClientIpUtils.getClientIp(httpServletRequest);
        try {
            return Result.success(authService.confirmEmailRegister(request, ip));
        } catch (BusinessException e) {
            log.warn("confirmEmailRegister failed, email={}, ip={}, message={}",
                request == null ? "" : request.getEmail(), ip, e.getMessage(), e);
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("confirmEmailRegister system error, email={}, ip={}",
                request == null ? "" : request.getEmail(), ip, e);
            return Result.error("注册失败，请稍后再试");
        }
    }

    /**
     * 邮箱 + 密码登录。
     */
    @PostMapping("/email/login")
    public Result<AuthLoginResponse> emailPasswordLogin(@RequestBody EmailPasswordLoginRequest request) {
        try {
            return Result.success(authService.emailPasswordLogin(request));
        } catch (BusinessException e) {
            log.warn("emailPasswordLogin failed, email={}, message={}",
                request == null ? "" : request.getEmail(), e.getMessage(), e);
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("emailPasswordLogin system error, email={}",
                request == null ? "" : request.getEmail(), e);
            return Result.error("登录失败，请稍后再试");
        }
    }

    /**
     * 申请邮箱找回/设置密码验证码。
     */
    @PostMapping("/email/password-reset/apply")
    public Result<String> applyEmailResetPasswordCode(
        @RequestBody EmailCodeApplyRequest request,
        HttpServletRequest httpServletRequest
    ) {
        String ip = ClientIpUtils.getClientIp(httpServletRequest);
        try {
            authService.applyEmailResetPasswordCode(request, ip);
            return Result.success("验证码已发送，请查收邮件（含垃圾箱）");
        } catch (BusinessException e) {
            log.warn("applyEmailResetPasswordCode failed, email={}, ip={}, message={}",
                request == null ? "" : request.getEmail(), ip, e.getMessage(), e);
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("applyEmailResetPasswordCode system error, email={}, ip={}",
                request == null ? "" : request.getEmail(), ip, e);
            return Result.error("验证码发送失败，请稍后再试");
        }
    }

    /**
     * 确认邮箱找回/设置密码（无密码用户也可设密）。
     */
    @PostMapping("/email/password-reset/confirm")
    public Result<String> confirmEmailResetPassword(
        @RequestBody EmailResetPasswordConfirmRequest request,
        HttpServletRequest httpServletRequest
    ) {
        String ip = ClientIpUtils.getClientIp(httpServletRequest);
        try {
            authService.confirmEmailResetPassword(request, ip);
            return Result.success("密码已更新，请使用新密码登录");
        } catch (BusinessException e) {
            log.warn("confirmEmailResetPassword failed, email={}, ip={}, message={}",
                request == null ? "" : request.getEmail(), ip, e.getMessage(), e);
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("confirmEmailResetPassword system error, email={}, ip={}",
                request == null ? "" : request.getEmail(), ip, e);
            return Result.error("密码更新失败，请稍后再试");
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
