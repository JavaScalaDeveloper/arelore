package com.arelore.server.user.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.arelore.server.user.dto.WechatQrCodeResponse;
import com.arelore.server.user.dto.WechatQrCodeStatusResponse;
import com.arelore.server.user.dto.WechatQuickLoginRequest;
import com.arelore.server.user.entity.QrCodeScene;
import com.arelore.server.user.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户认证服务实现类
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Value("${wechat.appid:YOUR_APPID}")
    private String wechatAppId;
    
    @Value("${wechat.redirect-uri:http%3a%2f%2fyoursite.com%2fcallback}")
    private String wechatRedirectUri;
    
    @Value("${wechat.scope:snsapi_login}")
    private String wechatScope;

    // 模拟存储二维码场景信息（实际应该使用 Redis）
    private static final Map<String, QrCodeScene> QR_CODE_SCENES = new ConcurrentHashMap<>();
    
    // 模拟存储用户 Token（实际应该使用 JWT + Redis）
    private static final Map<String, String> USER_TOKENS = new ConcurrentHashMap<>();

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
    public Map<String, Object> wechatQuickLogin(WechatQuickLoginRequest request) {
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
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", token);
        
        // 用户信息
        Map<String, Object> user = new HashMap<>();
        user.put("id", openid);
        user.put("username", request.getUserInfo() != null ? 
            request.getUserInfo().getNickname() : "微信用户");
        user.put("nickname", request.getUserInfo() != null ? 
            request.getUserInfo().getNickname() : "微信用户");
        user.put("avatar", request.getUserInfo() != null ? 
            request.getUserInfo().getAvatar() : "https://wx.qlogo.cn/mmopen/vi_32/DEFAULT");
        
        responseData.put("user", user);
        
        log.info("微信登录成功，userId: {}", openid);
        return responseData;
    }

    @Override
    public void logout(String token) {
        log.info("退出登录");
        
        if (token != null && token.startsWith("Bearer ")) {
            USER_TOKENS.remove(token);
        }
    }

    @Override
    public Map<String, Object> getCurrentUser(String token) {
        log.info("获取当前用户信息，token: {}", token);
        
        if (token == null || !USER_TOKENS.containsKey(token)) {
            // 返回 null，由 Controller层处理并返回错误
            return null;
        }
        
        // 获取用户信息（实际应该从数据库或缓存中获取）
        String openid = USER_TOKENS.get(token);
        
        Map<String, Object> user = new HashMap<>();
        user.put("id", openid);
        user.put("username", "微信用户");
        user.put("nickname", "微信用户");
        user.put("avatar", "https://wx.qlogo.cn/mmopen/vi_32/DEFAULT");
        
        return user;
    }
}
