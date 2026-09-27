package com.arelore.server.core.biz.user;

import com.arelore.server.core.biz.user.dto.WechatQrCodeResponse;
import com.arelore.server.core.biz.user.dto.WechatQrCodeStatusResponse;
import com.arelore.server.core.biz.user.dto.WechatQuickLoginRequest;
import com.arelore.server.core.biz.user.dto.MobileLoginRequest;
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
