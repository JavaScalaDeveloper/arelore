package com.arelore.server.core.biz.user;

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

/**
 * 用户认证服务接口
 */
public interface AuthService {

    /**
     * 获取微信扫码二维码
     *
     * @return 二维码信息
     */
    WechatQrCodeResponse getWechatQrCode();

    /**
     * 检查微信扫码状态
     *
     * @param sceneId 场景 ID
     * @return 二维码状态信息
     */
    WechatQrCodeStatusResponse checkWechatQrCodeStatus(String sceneId);

    /**
     * 微信一键登录
     *
     * @param request 登录请求
     * @return 登录结果（包含 token 和用户信息）
     */
    AuthLoginResponse wechatQuickLogin(WechatQuickLoginRequest request);

    /**
     * 移动端账号密码登录（测试）
     *
     * @param request 登录请求
     * @return 登录结果（包含 token 和用户信息）
     */
    AuthLoginResponse mobileLogin(MobileLoginRequest request);

    /**
     * 申请邮箱登录验证码（未注册邮箱也可申请，登录时自动注册）。
     */
    void applyEmailLoginCode(EmailCodeApplyRequest request, String clientIp);

    /**
     * 邮箱验证码登录；账号不存在时自动注册 EMAIL 账号（无密码）。
     */
    AuthLoginResponse emailCodeLogin(EmailCodeLoginRequest request, String clientIp);

    /**
     * 申请邮箱注册验证码（已注册邮箱拒绝）。
     */
    void applyEmailRegisterCode(EmailCodeApplyRequest request, String clientIp);

    /**
     * 邮箱注册确认：校验验证码并设置密码。
     */
    AuthLoginResponse confirmEmailRegister(EmailRegisterConfirmRequest request, String clientIp);

    /**
     * 邮箱 + 密码登录；未设置密码时明确报错。
     */
    AuthLoginResponse emailPasswordLogin(EmailPasswordLoginRequest request);

    /**
     * 申请邮箱找回/设置密码验证码（账号须已存在）。
     */
    void applyEmailResetPasswordCode(EmailCodeApplyRequest request, String clientIp);

    /**
     * 确认邮箱找回/设置密码（无密码用户也可通过此接口设密）。
     */
    void confirmEmailResetPassword(EmailResetPasswordConfirmRequest request, String clientIp);

    /**
     * 退出登录
     *
     * @param token 用户 token
     */
    void logout(String token);

    /**
     * 获取当前用户信息
     *
     * @param token 用户 token
     * @return 用户信息
     */
    AuthUserInfoResponse getCurrentUser(String token);
}
