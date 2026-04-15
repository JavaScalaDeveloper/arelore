package com.arelore.server.user.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arelore.server.core.registration.entity.UserRegistrationResult;
import com.arelore.server.core.registration.enums.AccountTypeEnum;
import com.arelore.server.core.registration.mapper.UserRegistrationResultMapper;
import com.arelore.server.core.registration.support.PasswordHashUtils;
import com.arelore.server.user.dto.AuthLoginResponse;
import com.arelore.server.user.dto.AuthUserInfoResponse;
import com.arelore.server.user.dto.WechatQrCodeResponse;
import com.arelore.server.user.dto.WechatQrCodeStatusResponse;
import com.arelore.server.user.dto.WechatQuickLoginRequest;
import com.arelore.server.user.dto.MobileLoginRequest;
import com.arelore.server.user.entity.QrCodeScene;
import com.arelore.server.user.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户认证服务实现类
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    /**
     * 用户注册结果表访问器，用于手机号登录校验。
     */
    private final UserRegistrationResultMapper userRegistrationResultMapper;

    public AuthServiceImpl(UserRegistrationResultMapper userRegistrationResultMapper) {
        this.userRegistrationResultMapper = userRegistrationResultMapper;
    }


    @Value("${wechat.appid:YOUR_APPID}")
    private String wechatAppId;
    
    @Value("${wechat.redirect-uri:http%3a%2f%2fyoursite.com%2fcallback}")
    private String wechatRedirectUri;
    
    @Value("${wechat.scope:snsapi_login}")
    private String wechatScope;

    @Value("${mobile.test-login.username:testuser}")
    private String mobileTestUsername;

    @Value("${mobile.test-login.password:123456}")
    private String mobileTestPassword;

    // 模拟存储二维码场景信息（实际应该使用 Redis）
    private static final Map<String, QrCodeScene> QR_CODE_SCENES = new ConcurrentHashMap<>();
    
    // 模拟存储用户 Token（实际应该使用 JWT + Redis）
    private static final ConcurrentHashMap<String, String> USER_TOKENS = new ConcurrentHashMap<>();
    /**
     * 维护 token 与用户展示信息的映射，避免查询当前用户时重复组装。
     */
    private static final ConcurrentHashMap<String, AuthUserInfoResponse> TOKEN_USER_INFOS = new ConcurrentHashMap<>();

    @Override
    public WechatQrCodeResponse getWechatQrCode() {
        log.info("生成微信扫码二维码");
        
        // 生成场景 ID
        String sceneId = IdUtil.fastSimpleUUID();
        
        try {
            // 使用配置文件中的参数生成二维码 URL
            String qrCodeUrl = "https://open.weixin.qq.com/connect/qrconnect?" +
                "appid=" + wechatAppId + "&" +
                "redirect_uri=" + wechatRedirectUri + "&" +
                "response_type=code&" +
                "scope=" + wechatScope + "&" +
                "state=" + sceneId +
                "#wechat_redirect";
            
            log.info("微信配置 - AppID: {}, RedirectURI: {}, Scope: {}", 
                wechatAppId, wechatRedirectUri, wechatScope);
            
            // 保存场景信息
            QrCodeScene scene = new QrCodeScene();
            scene.setSceneId(sceneId);
            scene.setStatus("WAIT"); // 等待扫码
            scene.setCreateTime(System.currentTimeMillis());
            scene.setExpireTime(System.currentTimeMillis() + 300000); // 5 分钟过期
            QR_CODE_SCENES.put(sceneId, scene);
            
            // 返回二维码信息
            WechatQrCodeResponse response = new WechatQrCodeResponse();
            response.setQrCodeUrl(qrCodeUrl);
            response.setSceneId(sceneId);
            response.setExpireSeconds(300);
            
            log.info("二维码生成成功，sceneId: {}", sceneId);
            return response;
            
        } catch (Exception e) {
            log.error("生成二维码失败", e);
            throw new RuntimeException("生成二维码失败：" + e.getMessage());
        }
    }

    @Override
    public WechatQrCodeStatusResponse checkWechatQrCodeStatus(String sceneId) {
        log.info("检查二维码状态，sceneId: {}", sceneId);
        
        if (sceneId == null || sceneId.isEmpty()) {
            throw new IllegalArgumentException("场景 ID 不能为空");
        }
        
        QrCodeScene scene = QR_CODE_SCENES.get(sceneId);
        if (scene == null) {
            // 二维码不存在或已过期
            WechatQrCodeStatusResponse response = new WechatQrCodeStatusResponse();
            response.setStatus("EXPIRED");
            return response;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > scene.getExpireTime()) {
            scene.setStatus("EXPIRED");
            WechatQrCodeStatusResponse response = new WechatQrCodeStatusResponse();
            response.setStatus("EXPIRED");
            return response;
        }
        
        // 模拟扫码状态（实际应该查询微信服务器）
        // 这里为了演示，随机返回状态
        WechatQrCodeStatusResponse response = new WechatQrCodeStatusResponse();
        response.setStatus(scene.getStatus());
        
        if ("CONFIRMED".equals(scene.getStatus())) {
            // 用户已确认登录，返回用户信息和授权码
            String authCode = IdUtil.fastSimpleUUID();
            
            WechatQrCodeStatusResponse.WechatUserInfo userInfo = 
                new WechatQrCodeStatusResponse.WechatUserInfo();
            userInfo.setOpenid("mock_openid_" + RandomUtil.randomNumbers(6));
            userInfo.setNickname("微信用户" + RandomUtil.randomNumbers(4));
            userInfo.setAvatar("https://wx.qlogo.cn/mmopen/vi_32/DEFAULT");
            
            response.setUserInfo(userInfo);
            response.setCode(authCode);
            
            // 清理场景
            QR_CODE_SCENES.remove(sceneId);
        }
        
        return response;
    }

    @Override
    public AuthLoginResponse wechatQuickLogin(WechatQuickLoginRequest request) {
        log.info("微信一键登录，openid: {}", 
            request.getUserInfo() != null ? request.getUserInfo().getOpenid() : "unknown");
        
        // 验证授权码（实际应该调用微信 API 验证）
        if (request.getCode() == null || request.getCode().isEmpty()) {
            throw new IllegalArgumentException("授权码不能为空");
        }
        
        // 获取或创建用户
        String openid = request.getUserInfo() != null ? 
            request.getUserInfo().getOpenid() : "default_openid";
        
        // 生成 Token（实际应该使用 JWT）
        String token = "Bearer " + IdUtil.fastSimpleUUID();
        
        // 保存 Token（实际应该存入 Redis）
        USER_TOKENS.put(token, openid);
        
        // 构建响应
        AuthUserInfoResponse user = new AuthUserInfoResponse();
        user.setId(openid);
        user.setUsername(request.getUserInfo() != null ? request.getUserInfo().getNickname() : "微信用户");
        user.setNickname(request.getUserInfo() != null ? request.getUserInfo().getNickname() : "微信用户");
        user.setAvatar(request.getUserInfo() != null ? request.getUserInfo().getAvatar() : "https://wx.qlogo.cn/mmopen/vi_32/DEFAULT");

        AuthLoginResponse responseData = new AuthLoginResponse();
        responseData.setToken(token);
        responseData.setUser(user);
        TOKEN_USER_INFOS.put(token, user);
        
        log.info("微信登录成功，userId: {}", openid);
        return responseData;
    }

    @Override
    public AuthLoginResponse mobileLogin(MobileLoginRequest request) {
        // 1) 基础参数校验
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("用户名或密码不能为空");
        }

        String username = request.getUsername().trim();
        String password = request.getPassword().trim();
        if (username.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("用户名或密码不能为空");
        }

        // 2) 先判断是否命中测试账号；测试账号与正式账号分别走不同校验逻辑
        AuthUserInfoResponse user;
        String userId;
        if (mobileTestUsername.equals(username)) {
            // 测试账号：走测试密码逻辑
            if (!mobileTestPassword.equals(password)) {
                throw new IllegalArgumentException("用户名或密码错误");
            }
            userId = "mobile_" + username;
            user = new AuthUserInfoResponse();
            user.setId(userId);
            user.setUsername(username);
            user.setNickname("移动端测试用户");
            user.setAvatar("");
        } else {
            // 非测试账号：从注册结果表读取并校验密码哈希
            UserRegistrationResult result = findMobileAccount(username);
            verifyMobilePassword(result, password);
            BigDecimal dbUserId = result.getUserId();
            if (dbUserId == null) {
                throw new IllegalArgumentException("用户名或密码错误");
            }
            userId = dbUserId.toPlainString();
            user = new AuthUserInfoResponse();
            user.setId(userId);
            user.setUsername(username);
            user.setNickname(username);
            user.setAvatar("");
        }

        // 3) 生成 token 并写入登录态缓存
        String token = "Bearer " + IdUtil.fastSimpleUUID();
        USER_TOKENS.put(token, userId);
        TOKEN_USER_INFOS.put(token, user);
        AuthLoginResponse responseData = new AuthLoginResponse();
        responseData.setToken(token);
        responseData.setUser(user);
        return responseData;
    }

    @Override
    public void logout(String token) {
        log.info("退出登录");
        
        if (token != null && token.startsWith("Bearer ")) {
            USER_TOKENS.remove(token);
            TOKEN_USER_INFOS.remove(token);
        }
    }

    @Override
    public AuthUserInfoResponse getCurrentUser(String token) {
        log.info("获取当前用户信息，token: {}", token);
        
        if (token == null || !USER_TOKENS.containsKey(token)) {
            // 返回 null，由 Controller层处理并返回错误
            return null;
        }

        AuthUserInfoResponse cached = TOKEN_USER_INFOS.get(token);
        if (cached != null) {
            return cached;
        }
        String userId = USER_TOKENS.get(token);
        AuthUserInfoResponse user = new AuthUserInfoResponse();
        user.setId(userId);
        user.setUsername("用户");
        user.setNickname("用户");
        user.setAvatar("");
        return user;
    }

    /**
     * 按手机号查询可登录账号（手机号账号类型 + 正常状态）。
     */
    private UserRegistrationResult findMobileAccount(String mobile) {
        LambdaQueryWrapper<UserRegistrationResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRegistrationResult::getAccountType, AccountTypeEnum.MOBILE.code())
            .eq(UserRegistrationResult::getAccount, mobile)
            .eq(UserRegistrationResult::getStatus, 1)
            .orderByDesc(UserRegistrationResult::getId)
            .last("limit 1");
        UserRegistrationResult result = userRegistrationResultMapper.selectOne(wrapper);
        if (result == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        return result;
    }

    /**
     * 校验手机号账号密码：
     * 从 ext_info 读取 passwordSalt/passwordHash，与当前输入密码做同算法比对。
     */
    private void verifyMobilePassword(UserRegistrationResult result, String password) {
        if (result.getExtInfo() == null || result.getExtInfo().isEmpty()) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        JSONObject ext;
        try {
            ext = JSON.parseObject(result.getExtInfo());
        } catch (Exception e) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        String salt = ext == null ? "" : ext.getString("passwordSalt");
        String expectedHash = ext == null ? "" : ext.getString("passwordHash");
        if (salt.isEmpty() || expectedHash.isEmpty()) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        String actualHash = PasswordHashUtils.sha256(salt + ":" + password);
        if (!expectedHash.equals(actualHash)) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
    }
}
