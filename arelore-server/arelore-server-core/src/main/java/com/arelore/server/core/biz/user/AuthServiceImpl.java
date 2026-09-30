package com.arelore.server.core.biz.user;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arelore.server.core.registration.entity.UserRegistrationApplication;
import com.arelore.server.core.registration.entity.UserRegistrationResult;
import com.arelore.server.core.registration.enums.AccountTypeEnum;
import com.arelore.server.core.registration.mapper.UserRegistrationApplicationMapper;
import com.arelore.server.core.registration.mapper.UserRegistrationResultMapper;
import com.arelore.server.core.registration.support.PasswordHashUtils;
import com.arelore.server.core.biz.user.dto.AuthLoginResponse;
import com.arelore.server.core.biz.user.dto.AuthUserInfoResponse;
import com.arelore.server.core.biz.user.dto.WechatQrCodeResponse;
import com.arelore.server.core.biz.user.dto.WechatQrCodeStatusResponse;
import com.arelore.server.core.biz.user.dto.WechatQuickLoginRequest;
import com.arelore.server.core.biz.user.dto.MobileLoginRequest;
import com.arelore.server.core.biz.user.entity.QrCodeScene;
import com.arelore.server.core.biz.user.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户认证服务实现类
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    private static final String ACCOUNT_TYPE_WECHAT = AccountTypeEnum.WECHAT.code();
    /**
     * 用户注册结果表访问器，用于手机号登录校验。
     */
    private final UserRegistrationApplicationMapper userRegistrationApplicationMapper;
    private final UserRegistrationResultMapper userRegistrationResultMapper;
    private final UserAuthSessionService userAuthSessionService;

    public AuthServiceImpl(
        UserRegistrationApplicationMapper userRegistrationApplicationMapper,
        UserRegistrationResultMapper userRegistrationResultMapper,
        UserAuthSessionService userAuthSessionService
    ) {
        this.userRegistrationApplicationMapper = userRegistrationApplicationMapper;
        this.userRegistrationResultMapper = userRegistrationResultMapper;
        this.userAuthSessionService = userAuthSessionService;
    }


    @Value("${wechat.appid:YOUR_APPID}")
    private String wechatAppId;
    
    @Value("${wechat.redirect-uri:http%3a%2f%2fyoursite.com%2fcallback}")
    private String wechatRedirectUri;
    
    @Value("${wechat.scope:snsapi_login}")
    private String wechatScope;

    /**
     * 微信小程序 code2session 配置（用于获取真实 openid，保证同一用户不会反复注册）。
     * 若不配置，则降级使用前端传入的 openid（仅适用于本地调试）。
     */
    @Value("${wechat.mini.appid:}")
    private String wechatMiniAppId;

    @Value("${wechat.mini.secret:}")
    private String wechatMiniSecret;

    @Value("${mobile.test-login.username:testuser}")
    private String mobileTestUsername;

    @Value("${mobile.test-login.password:123456}")
    private String mobileTestPassword;

    // 模拟存储二维码场景信息（实际应该使用 Redis）
    private static final Map<String, QrCodeScene> QR_CODE_SCENES = new ConcurrentHashMap<>();

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
        log.info("微信一键登录，requestOpenid: {}",
            request.getUserInfo() != null ? request.getUserInfo().getOpenid() : "null");
        
        // 验证授权码（实际应该调用微信 API 验证）
        if (request.getCode() == null || request.getCode().isEmpty()) {
            throw new IllegalArgumentException("授权码不能为空");
        }
        
        String requestOpenid = request.getUserInfo() != null ? request.getUserInfo().getOpenid() : "";
        requestOpenid = requestOpenid == null ? "" : requestOpenid.trim();

        // 小程序登录：优先通过 code2session 获取真实 openid（稳定且可判重），否则降级使用前端 openid（仅本地调试）
        String openid = resolveMiniOpenidByCode2Session(request.getCode(), requestOpenid);
        openid = openid == null ? "" : openid.trim();
        if (openid.isEmpty()) {
            throw new IllegalArgumentException("openid不能为空");
        }
        log.info("微信一键登录，resolvedOpenid={}", openid);

        // 微信一键登录成功后自动注册兜底：
        // 若 application/result 表中不存在该微信账号数据，则自动插入。
        ensureWechatAutoRegistered(openid, request);
        
        // 生成 Token，落库 + 本地缓存（重启可恢复）
        String token = "Bearer " + IdUtil.fastSimpleUUID();
        
        // 构建响应
        AuthUserInfoResponse user = new AuthUserInfoResponse();
        user.setId(openid);
        user.setUsername(request.getUserInfo() != null ? request.getUserInfo().getNickname() : "微信用户");
        user.setNickname(request.getUserInfo() != null ? request.getUserInfo().getNickname() : "微信用户");
        user.setAvatar(request.getUserInfo() != null ? request.getUserInfo().getAvatar() : "https://wx.qlogo.cn/mmopen/vi_32/DEFAULT");

        userAuthSessionService.issue(token, user);

        AuthLoginResponse responseData = new AuthLoginResponse();
        responseData.setToken(token);
        responseData.setUser(user);
        
        log.info("微信登录成功，userId: {}", openid);
        return responseData;
    }

    /**
     * 小程序登录：通过微信 code2session 获取 openid（真实环境），避免前端 openid 不稳定导致重复注册。
     * 若未配置小程序 appid/secret 或调用失败，则降级使用前端传入的 openid（仅本地调试）。
     */
    private String resolveMiniOpenidByCode2Session(String jsCode, String fallbackOpenid) {
        String appid = wechatMiniAppId == null ? "" : wechatMiniAppId.trim();
        String secret = wechatMiniSecret == null ? "" : wechatMiniSecret.trim();

        if (!appid.isEmpty() && !secret.isEmpty()) {
            try {
                String url = "https://api.weixin.qq.com/sns/jscode2session" +
                    "?appid=" + appid +
                    "&secret=" + secret +
                    "&js_code=" + (jsCode == null ? "" : jsCode.trim()) +
                    "&grant_type=authorization_code";
                String resp = HttpUtil.get(url);
                JSONObject json = JSON.parseObject(resp);
                String openid = json == null ? "" : json.getString("openid");
                openid = openid == null ? "" : openid.trim();
                if (!openid.isEmpty()) {
                    return openid;
                }
                String errcode = json == null ? "" : json.getString("errcode");
                String errmsg = json == null ? "" : json.getString("errmsg");
                log.warn("code2session did not return openid, errcode={}, errmsg={}", errcode, errmsg);
            } catch (Exception e) {
                log.warn("code2session call failed, fallback to request openid. message={}", e.getMessage(), e);
            }
        }

        return fallbackOpenid == null ? "" : fallbackOpenid.trim();
    }

    /**
     * 检查并自动补齐微信账号的注册流水与注册结果。
     */
    private void ensureWechatAutoRegistered(String openid, WechatQuickLoginRequest request) {
        if (openid == null || openid.isBlank()) {
            return;
        }
        openid = openid.trim();
        if (openid.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<UserRegistrationApplication> appWrapper = new LambdaQueryWrapper<>();
        appWrapper.eq(UserRegistrationApplication::getAccountType, ACCOUNT_TYPE_WECHAT)
            .eq(UserRegistrationApplication::getAccount, openid)
            .last("limit 1");
        UserRegistrationApplication existingApp = userRegistrationApplicationMapper.selectOne(appWrapper);
        if (existingApp == null) {
            UserRegistrationApplication app = new UserRegistrationApplication();
            app.setAccountType(ACCOUNT_TYPE_WECHAT);
            app.setAccount(openid);
            app.setClientIp("wechat_quick_login");
            JSONObject ext = new JSONObject();
            ext.put("scene", "WECHAT_QUICK_LOGIN");
            if (request != null && request.getUserInfo() != null) {
                ext.put("nickname", request.getUserInfo().getNickname());
                ext.put("avatar", request.getUserInfo().getAvatar());
            }
            app.setExtInfo(JSON.toJSONString(ext));
            userRegistrationApplicationMapper.insert(app);
        }

        LambdaQueryWrapper<UserRegistrationResult> resultWrapper = new LambdaQueryWrapper<>();
        resultWrapper.eq(UserRegistrationResult::getAccountType, ACCOUNT_TYPE_WECHAT)
            .eq(UserRegistrationResult::getAccount, openid)
            .orderByDesc(UserRegistrationResult::getId)
            .last("limit 1");
        UserRegistrationResult existingResult = userRegistrationResultMapper.selectOne(resultWrapper);
        if (existingResult != null) {
            log.info("微信自动注册：账号已存在，skip insert. openid={}, id={}", openid, existingResult.getId());
            // 若用户信息有更新（头像/昵称），尽量同步到 ext_info，不新增账号记录
            if (request != null && request.getUserInfo() != null) {
                try {
                    String extInfo = existingResult.getExtInfo();
                    JSONObject ext = extInfo == null || extInfo.isBlank() ? new JSONObject() : JSON.parseObject(extInfo);
                    ext.put("source", "WECHAT_QUICK_LOGIN");
                    ext.put("nickname", request.getUserInfo().getNickname());
                    ext.put("avatar", request.getUserInfo().getAvatar());
                    ext.put("gender", request.getUserInfo().getGender());
                    ext.put("country", request.getUserInfo().getCountry());
                    ext.put("province", request.getUserInfo().getProvince());
                    ext.put("city", request.getUserInfo().getCity());
                    existingResult.setExtInfo(JSON.toJSONString(ext));
                    userRegistrationResultMapper.updateById(existingResult);
                } catch (Exception ignore) {
                    // ext_info 更新失败不影响登录
                }
            }
            return;
        }

        JSONObject resultExt = new JSONObject();
        resultExt.put("source", "WECHAT_QUICK_LOGIN");
        if (request != null && request.getUserInfo() != null) {
            resultExt.put("nickname", request.getUserInfo().getNickname());
            resultExt.put("avatar", request.getUserInfo().getAvatar());
            resultExt.put("gender", request.getUserInfo().getGender());
            resultExt.put("country", request.getUserInfo().getCountry());
            resultExt.put("province", request.getUserInfo().getProvince());
            resultExt.put("city", request.getUserInfo().getCity());
        }

        for (int i = 0; i < 5; i++) {
            UserRegistrationResult result = new UserRegistrationResult();
            result.setUserId(new BigDecimal(generateCrawlerResistantUserId()));
            result.setMergedToUserId(null);
            result.setAccountType(ACCOUNT_TYPE_WECHAT);
            result.setAccount(openid);
            result.setStatus(1);
            result.setExtInfo(JSON.toJSONString(resultExt));
            try {
                userRegistrationResultMapper.insert(result);
                return;
            } catch (DuplicateKeyException e) {
                // user_id 或 account 冲突时重试
            }
        }
        log.warn("微信自动注册重试后仍失败，openid={}", openid);
    }

    @Override
    public AuthLoginResponse mobileLogin(MobileLoginRequest request) {
        // 1) 基础参数校验
        if (request == null || request.getUsername() == null) {
            throw new IllegalArgumentException("用户名或密码不能为空");
        }

        String username = request.getUsername().trim();
        List<String> passwordCredentials = collectCredentials(request);
        if (username.isEmpty() || passwordCredentials.isEmpty()) {
            throw new IllegalArgumentException("用户名或密码不能为空");
        }

        // 2) 先判断是否命中测试账号；测试账号与正式账号分别走不同校验逻辑
        AuthUserInfoResponse user;
        String userId;
        if (mobileTestUsername.equals(username)) {
            // 测试账号：兼容摘要口令与旧版明文口令
            if (!verifyTestPassword(passwordCredentials)) {
                throw new IllegalArgumentException("密码错误");
            }
            userId = "mobile_" + username;
            user = new AuthUserInfoResponse();
            user.setId(userId);
            user.setUsername(username);
            user.setNickname("移动端测试用户");
            user.setAvatar("");
        } else {
            // 非测试账号：从注册结果表读取并校验密码（兼容多口令格式）
            UserRegistrationResult result = findMobileAccount(username);
            verifyMobilePassword(result, passwordCredentials);
            BigDecimal dbUserId = result.getUserId();
            if (dbUserId == null) {
                throw new IllegalArgumentException("用户不存在");
            }
            userId = dbUserId.toPlainString();
            user = new AuthUserInfoResponse();
            user.setId(userId);
            user.setUsername(username);
            user.setNickname(username);
            user.setAvatar("");
        }

        // 3) 生成 token 并写入登录会话（MySQL + 本地缓存）
        String token = "Bearer " + IdUtil.fastSimpleUUID();
        userAuthSessionService.issue(token, user);
        AuthLoginResponse responseData = new AuthLoginResponse();
        responseData.setToken(token);
        responseData.setUser(user);
        return responseData;
    }

    @Override
    public void logout(String token) {
        log.info("退出登录");
        userAuthSessionService.invalidate(token);
    }

    @Override
    public AuthUserInfoResponse getCurrentUser(String token) {
        log.info("获取当前用户信息，token: {}", token);
        return userAuthSessionService.getValidUser(token);
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
            throw new IllegalArgumentException("用户不存在");
        }
        return result;
    }

    /**
     * 校验手机号账号密码：
     * 从 ext_info 读取 passwordSalt/passwordHash，与当前输入密码做同算法比对。
     */
    private void verifyMobilePassword(UserRegistrationResult result, List<String> passwordCredentials) {
        if (result.getExtInfo() == null || result.getExtInfo().isEmpty()) {
            throw new IllegalArgumentException("密码错误");
        }
        JSONObject ext;
        try {
            ext = JSON.parseObject(result.getExtInfo());
        } catch (Exception e) {
            throw new IllegalArgumentException("密码错误");
        }
        String salt = ext == null ? "" : ext.getString("passwordSalt");
        String expectedHash = ext == null ? "" : ext.getString("passwordHash");
        if (salt.isEmpty() || expectedHash.isEmpty()) {
            throw new IllegalArgumentException("密码错误");
        }

        // 兼容策略：
        // 1) 标准格式：sha256(salt + ":" + credential)
        // 2) 兜底格式：客户端直接传最终 hash（与库中 passwordHash 相等）
        for (String credential : passwordCredentials) {
            String actualHash = PasswordHashUtils.sha256(salt + ":" + credential);
            if (expectedHash.equals(actualHash) || expectedHash.equals(credential)) {
                return;
            }
        }
        throw new IllegalArgumentException("密码错误");
    }

    /**
     * 收集可用口令（passwordHash + password），用于兼容不同客户端版本。
     */
    private List<String> collectCredentials(MobileLoginRequest request) {
        Set<String> uniqueCredentials = new LinkedHashSet<>();
        if (request.getPasswordHash() != null && !request.getPasswordHash().trim().isEmpty()) {
            uniqueCredentials.add(request.getPasswordHash().trim());
        }
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            uniqueCredentials.add(request.getPassword().trim());
        }
        return new ArrayList<>(uniqueCredentials);
    }

    /**
     * 测试账号口令校验：
     * - 新版客户端传 sha256(明文)
     * - 旧版客户端可能仍传明文
     */
    private boolean verifyTestPassword(List<String> credentials) {
        String testPasswordHash = PasswordHashUtils.sha256(mobileTestPassword);
        for (String credential : credentials) {
            if (mobileTestPassword.equals(credential) || testPasswordHash.equals(credential)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成 20 位 user_id：2688 + 16位不含4数字。
     */
    private String generateCrawlerResistantUserId() {
        final char[] digitsWithoutFour = {'0', '1', '2', '3', '5', '6', '7', '8', '9'};
        StringBuilder sb = new StringBuilder("2688");
        for (int i = 0; i < 16; i++) {
            sb.append(digitsWithoutFour[RandomUtil.randomInt(digitsWithoutFour.length)]);
        }
        return sb.toString();
    }
}
