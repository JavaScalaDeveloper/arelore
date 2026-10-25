package com.arelore.server.core.biz.user;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arelore.server.core.common.constant.CommonConstants;
import com.arelore.server.core.common.exception.BusinessException;
import com.arelore.server.core.common.result.ResultCode;
import com.arelore.server.core.registration.entity.UserRegistrationApplication;
import com.arelore.server.core.registration.entity.UserRegistrationResult;
import com.arelore.server.core.registration.enums.AccountTypeEnum;
import com.arelore.server.core.registration.mail.EmailSender;
import com.arelore.server.core.registration.mapper.UserRegistrationApplicationMapper;
import com.arelore.server.core.registration.mapper.UserRegistrationResultMapper;
import com.arelore.server.core.registration.support.PasswordHashUtils;
import com.arelore.server.core.biz.user.dto.AuthLoginResponse;
import com.arelore.server.core.biz.user.dto.AuthUserInfoResponse;
import com.arelore.server.core.biz.user.dto.EmailCodeApplyRequest;
import com.arelore.server.core.biz.user.dto.EmailCodeLoginRequest;
import com.arelore.server.core.biz.user.dto.EmailRegisterConfirmRequest;
import com.arelore.server.core.biz.user.dto.EmailPasswordLoginRequest;
import com.arelore.server.core.biz.user.dto.EmailResetPasswordConfirmRequest;
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
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private static final String ACCOUNT_TYPE_EMAIL = AccountTypeEnum.EMAIL.code();
    private static final String ACCOUNT_TYPE_EMAIL_OTP_LOGIN = AccountTypeEnum.EMAIL_OTP_LOGIN.code();
    private static final String ACCOUNT_TYPE_EMAIL_REGISTER = AccountTypeEnum.EMAIL_REGISTER.code();
    private static final String ACCOUNT_TYPE_EMAIL_PASSWORD_RESET = AccountTypeEnum.EMAIL_PASSWORD_RESET.code();
    private static final int EMAIL_CODE_VALID_MINUTES = 5;
    /** 前端 sha256(明文) 后的十六进制长度 */
    private static final int EMAIL_PASSWORD_HASH_HEX_LEN = 64;
    private static final long EMAIL_APPLY_COOLDOWN_SECONDS = 90L;
    private static final long EMAIL_IP_COOLDOWN_MS = 60_000L;
    private static final long EMAIL_IP_WINDOW_MS = 60 * 60_000L;
    private static final int EMAIL_IP_MAX_PER_WINDOW = 30;

    private static final Map<String, Long> EMAIL_IP_LAST_APPLY_AT = new ConcurrentHashMap<>();
    private static final Map<String, EmailWindowCounter> EMAIL_IP_WINDOW_COUNTER = new ConcurrentHashMap<>();

    /**
     * 用户注册结果表访问器，用于手机号登录校验。
     */
    private final UserRegistrationApplicationMapper userRegistrationApplicationMapper;
    private final UserRegistrationResultMapper userRegistrationResultMapper;
    private final UserAuthSessionService userAuthSessionService;
    private final EmailSender emailSender;

    public AuthServiceImpl(
        UserRegistrationApplicationMapper userRegistrationApplicationMapper,
        UserRegistrationResultMapper userRegistrationResultMapper,
        UserAuthSessionService userAuthSessionService,
        EmailSender emailSender
    ) {
        this.userRegistrationApplicationMapper = userRegistrationApplicationMapper;
        this.userRegistrationResultMapper = userRegistrationResultMapper;
        this.userAuthSessionService = userAuthSessionService;
        this.emailSender = emailSender;
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
    public void applyEmailLoginCode(EmailCodeApplyRequest request, String clientIp) {
        String email = requireNormalizedEmail(request == null ? null : request.getEmail());
        clientIp = normalizeClientIp(clientIp);
        enforceEmailIpRateLimit(clientIp);
        enforceEmailApplyCooldown(email, ACCOUNT_TYPE_EMAIL_OTP_LOGIN);
        sendEmailVerifyCode(email, clientIp, ACCOUNT_TYPE_EMAIL_OTP_LOGIN, "LOGIN");
    }

    @Override
    public AuthLoginResponse emailCodeLogin(EmailCodeLoginRequest request, String clientIp) {
        String email = requireNormalizedEmail(request == null ? null : request.getEmail());
        String code = request == null || request.getCode() == null ? "" : request.getCode().trim();
        requireVerifyCodeFormat(code);
        consumeEmailVerifyCode(email, code, ACCOUNT_TYPE_EMAIL_OTP_LOGIN);
        UserRegistrationResult account = findOrCreateEmailAccount(email, normalizeClientIp(clientIp));
        return issueEmailSession(account);
    }

    @Override
    public void applyEmailRegisterCode(EmailCodeApplyRequest request, String clientIp) {
        String email = requireNormalizedEmail(request == null ? null : request.getEmail());
        clientIp = normalizeClientIp(clientIp);
        if (findEmailAccount(email) != null) {
            throw new BusinessException(ResultCode.USER_MOBILE_ALREADY_REGISTERED, "该邮箱已注册，请直接登录");
        }
        enforceEmailIpRateLimit(clientIp);
        enforceEmailApplyCooldown(email, ACCOUNT_TYPE_EMAIL_REGISTER);
        sendEmailVerifyCode(email, clientIp, ACCOUNT_TYPE_EMAIL_REGISTER, "REGISTER");
    }

    @Override
    public AuthLoginResponse confirmEmailRegister(EmailRegisterConfirmRequest request, String clientIp) {
        String email = requireNormalizedEmail(request == null ? null : request.getEmail());
        String code = request == null || request.getCode() == null ? "" : request.getCode().trim();
        String clientPasswordHash = request == null || request.getPasswordHash() == null
            ? "" : request.getPasswordHash().trim();
        clientIp = normalizeClientIp(clientIp);
        requireVerifyCodeFormat(code);
        requireClientPasswordHash(clientPasswordHash);
        if (findEmailAccount(email) != null) {
            throw new BusinessException(ResultCode.USER_MOBILE_ALREADY_REGISTERED, "该邮箱已注册，请直接登录");
        }
        consumeEmailVerifyCode(email, code, ACCOUNT_TYPE_EMAIL_REGISTER);

        String salt = PasswordHashUtils.randomSaltBase64Url(16);
        String storedHash = PasswordHashUtils.sha256(salt + ":" + clientPasswordHash);
        JSONObject resultExt = new JSONObject();
        resultExt.put("registerIp", clientIp);
        resultExt.put("registerAt", LocalDateTime.now().toString());
        resultExt.put("registerChannel", "EMAIL_REGISTER");
        resultExt.put("passwordSalt", salt);
        resultExt.put("passwordHash", storedHash);

        UserRegistrationResult account = insertEmailAccount(email, resultExt);
        return issueEmailSession(account);
    }

    @Override
    public AuthLoginResponse emailPasswordLogin(EmailPasswordLoginRequest request) {
        String email = requireNormalizedEmail(request == null ? null : request.getEmail());
        String clientPasswordHash = request == null || request.getPasswordHash() == null
            ? "" : request.getPasswordHash().trim();
        requireClientPasswordHash(clientPasswordHash);
        UserRegistrationResult account = findEmailAccount(email);
        if (account == null) {
            throw new BusinessException(ResultCode.USER_REGISTER_PARAM_INVALID, "用户不存在");
        }
        JSONObject ext = parseExtInfo(account.getExtInfo());
        String salt = ext.getString("passwordSalt");
        String expectedHash = ext.getString("passwordHash");
        if (!StringUtils.hasText(salt) || !StringUtils.hasText(expectedHash)) {
            throw new BusinessException(ResultCode.USER_REGISTER_PARAM_INVALID, "尚未设置密码，请先注册设密或通过邮箱验证码找回/设置密码");
        }
        String actualHash = PasswordHashUtils.sha256(salt + ":" + clientPasswordHash);
        if (!expectedHash.equals(actualHash)) {
            throw new BusinessException(ResultCode.USER_REGISTER_PARAM_INVALID, "密码错误");
        }
        return issueEmailSession(account);
    }

    @Override
    public void applyEmailResetPasswordCode(EmailCodeApplyRequest request, String clientIp) {
        String email = requireNormalizedEmail(request == null ? null : request.getEmail());
        clientIp = normalizeClientIp(clientIp);
        if (findEmailAccount(email) == null) {
            throw new BusinessException(ResultCode.USER_REGISTER_PARAM_INVALID, "该邮箱尚未注册");
        }
        enforceEmailIpRateLimit(clientIp);
        enforceEmailApplyCooldown(email, ACCOUNT_TYPE_EMAIL_PASSWORD_RESET);
        sendEmailVerifyCode(email, clientIp, ACCOUNT_TYPE_EMAIL_PASSWORD_RESET, "RESET_PASSWORD");
    }

    @Override
    public void confirmEmailResetPassword(EmailResetPasswordConfirmRequest request, String clientIp) {
        String email = requireNormalizedEmail(request == null ? null : request.getEmail());
        String code = request == null || request.getCode() == null ? "" : request.getCode().trim();
        String clientPasswordHash = request == null || request.getNewPasswordHash() == null
            ? "" : request.getNewPasswordHash().trim();
        clientIp = normalizeClientIp(clientIp);
        requireVerifyCodeFormat(code);
        requireClientPasswordHash(clientPasswordHash);
        UserRegistrationResult account = findEmailAccount(email);
        if (account == null) {
            throw new BusinessException(ResultCode.USER_REGISTER_PARAM_INVALID, "该邮箱尚未注册");
        }
        consumeEmailVerifyCode(email, code, ACCOUNT_TYPE_EMAIL_PASSWORD_RESET);

        JSONObject resultExt = parseExtInfo(account.getExtInfo());
        String salt = PasswordHashUtils.randomSaltBase64Url(16);
        String storedHash = PasswordHashUtils.sha256(salt + ":" + clientPasswordHash);
        resultExt.put("passwordSalt", salt);
        resultExt.put("passwordHash", storedHash);
        resultExt.put("passwordResetAt", LocalDateTime.now().toString());
        resultExt.put("passwordResetIp", clientIp);
        account.setExtInfo(JSON.toJSONString(resultExt));
        userRegistrationResultMapper.updateById(account);
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
            // 非测试账号：支持手机号 / 邮箱账号；按场景区分「不存在 / 停用 / 未设密 / 密码错误」
            UserRegistrationResult result = findPasswordLoginAccount(username);
            assertAccountActive(result);
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
     * 密码登录查账号：先按手机号（MOBILE），再按邮箱（EMAIL，忽略大小写）。
     * 不按 status 过滤，便于区分「不存在」与「已停用/封禁」。
     */
    private UserRegistrationResult findPasswordLoginAccount(String username) {
        UserRegistrationResult mobile = selectAccountByType(AccountTypeEnum.MOBILE.code(), username);
        if (mobile != null) {
            return mobile;
        }
        String email = normalizeEmail(username);
        if (StringUtils.hasText(email) && email.contains("@")) {
            UserRegistrationResult emailAccount = selectAccountByType(AccountTypeEnum.EMAIL.code(), email);
            if (emailAccount != null) {
                return emailAccount;
            }
        }
        throw new IllegalArgumentException("用户不存在");
    }

    private UserRegistrationResult selectAccountByType(String accountType, String account) {
        if (!StringUtils.hasText(accountType) || !StringUtils.hasText(account)) {
            return null;
        }
        LambdaQueryWrapper<UserRegistrationResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRegistrationResult::getAccountType, accountType)
            .eq(UserRegistrationResult::getAccount, account)
            .orderByDesc(UserRegistrationResult::getId)
            .last("limit 1");
        return userRegistrationResultMapper.selectOne(wrapper);
    }

    /** status: 1-正常 2-封禁 3-停用 */
    private void assertAccountActive(UserRegistrationResult account) {
        if (account == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        Integer status = account.getStatus();
        if (status == null) {
            throw new IllegalArgumentException("账号状态异常，请联系客服");
        }
        if (status == 1) {
            return;
        }
        if (status == 2) {
            throw new IllegalArgumentException("账号已被封禁");
        }
        if (status == 3) {
            throw new IllegalArgumentException("账号已停用");
        }
        throw new IllegalArgumentException("账号不可登录（状态=" + status + "）");
    }

    /**
     * 校验账号密码：
     * 从 ext_info 读取 passwordSalt/passwordHash，与当前输入密码做同算法比对。
     */
    private void verifyMobilePassword(UserRegistrationResult result, List<String> passwordCredentials) {
        if (result.getExtInfo() == null || result.getExtInfo().isEmpty()) {
            throw new IllegalArgumentException("尚未设置密码，请先注册设密或找回密码");
        }
        JSONObject ext;
        try {
            ext = JSON.parseObject(result.getExtInfo());
        } catch (Exception e) {
            throw new IllegalArgumentException("账号密码数据异常，请找回密码后重试");
        }
        String salt = ext == null ? "" : ext.getString("passwordSalt");
        String expectedHash = ext == null ? "" : ext.getString("passwordHash");
        if (!StringUtils.hasText(salt) || !StringUtils.hasText(expectedHash)) {
            throw new IllegalArgumentException("尚未设置密码，请先注册设密或找回密码");
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

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return "";
        }
        return email.trim().toLowerCase();
    }

    private String requireNormalizedEmail(String raw) {
        String email = normalizeEmail(raw);
        if (!StringUtils.hasText(email)) {
            throw new BusinessException(ResultCode.USER_REGISTER_PARAM_INVALID, "邮箱不能为空");
        }
        if (!email.matches(CommonConstants.EMAIL_REGEX)) {
            throw new BusinessException(ResultCode.USER_REGISTER_PARAM_INVALID, "邮箱格式不正确");
        }
        return email;
    }

    private String normalizeClientIp(String clientIp) {
        if (!StringUtils.hasText(clientIp) || "unknown".equalsIgnoreCase(clientIp)) {
            return "unknown";
        }
        return clientIp;
    }

    private void requireVerifyCodeFormat(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException(ResultCode.USER_REGISTER_PARAM_INVALID, "验证码不能为空");
        }
        if (!code.matches("^\\d{4,8}$")) {
            throw new BusinessException(ResultCode.USER_REGISTER_PARAM_INVALID, "验证码格式不正确");
        }
    }

    /**
     * 要求客户端已做 sha256(明文) 再传输，拒绝明文形态。
     */
    private void requireClientPasswordHash(String passwordHash) {
        if (!StringUtils.hasText(passwordHash)) {
            throw new BusinessException(ResultCode.USER_REGISTER_PARAM_INVALID, "密码摘要不能为空");
        }
        if (!passwordHash.matches("^[a-fA-F0-9]{" + EMAIL_PASSWORD_HASH_HEX_LEN + "}$")) {
            throw new BusinessException(ResultCode.USER_REGISTER_PARAM_INVALID, "密码须经客户端摘要后传输");
        }
    }

    private JSONObject parseExtInfo(String raw) {
        if (!StringUtils.hasText(raw)) {
            return new JSONObject();
        }
        try {
            JSONObject ext = JSON.parseObject(raw);
            return ext == null ? new JSONObject() : ext;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private void sendEmailVerifyCode(String email, String clientIp, String accountType, String purpose) {
        String verifyCode = RandomUtil.randomNumbers(6);
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(EMAIL_CODE_VALID_MINUTES);
        JSONObject ext = new JSONObject();
        ext.put("purpose", purpose);
        ext.put("verifyCode", verifyCode);
        ext.put("codeExpireAt", expireAt.toString());
        ext.put("codeValidMinutes", EMAIL_CODE_VALID_MINUTES);

        UserRegistrationApplication application = new UserRegistrationApplication();
        application.setAccountType(accountType);
        application.setAccount(email);
        application.setClientIp(clientIp);
        application.setExtInfo(JSON.toJSONString(ext));
        userRegistrationApplicationMapper.insert(application);

        try {
            emailSender.sendVerifyCode(email, verifyCode, EMAIL_CODE_VALID_MINUTES);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("sendEmailVerifyCode failed, email={}, purpose={}, message={}", email, purpose, e.getMessage(), e);
            throw new BusinessException(ResultCode.USER_REGISTER_APPLY_FAILED, "验证码发送失败，请稍后重试");
        }
    }

    private void consumeEmailVerifyCode(String email, String code, String accountType) {
        LambdaQueryWrapper<UserRegistrationApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRegistrationApplication::getAccountType, accountType)
            .eq(UserRegistrationApplication::getAccount, email)
            .orderByDesc(UserRegistrationApplication::getId)
            .last("limit 1");
        UserRegistrationApplication latest = userRegistrationApplicationMapper.selectOne(wrapper);
        if (latest == null || !StringUtils.hasText(latest.getExtInfo())) {
            throw new BusinessException(ResultCode.USER_REGISTER_PARAM_INVALID, "请先获取验证码");
        }
        JSONObject ext = parseExtInfo(latest.getExtInfo());
        if (Boolean.TRUE.equals(ext.getBoolean("used"))) {
            throw new BusinessException(ResultCode.USER_REGISTER_PARAM_INVALID, "验证码已使用，请重新获取");
        }
        String expected = ext.getString("verifyCode");
        String expireAtStr = ext.getString("codeExpireAt");
        if (!StringUtils.hasText(expected) || !StringUtils.hasText(expireAtStr)) {
            throw new BusinessException(ResultCode.USER_REGISTER_PARAM_INVALID, "验证码信息缺失，请重新获取");
        }
        if (!expected.equals(code)) {
            throw new BusinessException(ResultCode.USER_REGISTER_PARAM_INVALID, "验证码错误");
        }
        try {
            LocalDateTime expireAt = LocalDateTime.parse(expireAtStr);
            if (LocalDateTime.now().isAfter(expireAt)) {
                throw new BusinessException(ResultCode.USER_REGISTER_PARAM_INVALID, "验证码已过期，请重新获取");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.USER_REGISTER_PARAM_INVALID, "验证码过期时间异常，请重新获取");
        }
        ext.put("used", true);
        ext.put("usedAt", LocalDateTime.now().toString());
        latest.setExtInfo(JSON.toJSONString(ext));
        userRegistrationApplicationMapper.updateById(latest);
    }

    private void enforceEmailApplyCooldown(String email, String accountType) {
        LambdaQueryWrapper<UserRegistrationApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRegistrationApplication::getAccountType, accountType)
            .eq(UserRegistrationApplication::getAccount, email)
            .orderByDesc(UserRegistrationApplication::getId)
            .last("limit 1");
        UserRegistrationApplication latest = userRegistrationApplicationMapper.selectOne(wrapper);
        if (latest == null || latest.getCreateTime() == null) {
            return;
        }
        LocalDateTime nextAllowed = latest.getCreateTime().plusSeconds(EMAIL_APPLY_COOLDOWN_SECONDS);
        if (LocalDateTime.now().isBefore(nextAllowed)) {
            throw new BusinessException(ResultCode.USER_REGISTER_RATE_LIMITED, "验证码发送过于频繁，请稍后再试");
        }
    }

    private void enforceEmailIpRateLimit(String clientIp) {
        long now = System.currentTimeMillis();
        Long last = EMAIL_IP_LAST_APPLY_AT.get(clientIp);
        if (last != null && now - last < EMAIL_IP_COOLDOWN_MS) {
            throw new BusinessException(ResultCode.USER_REGISTER_RATE_LIMITED, "操作过于频繁，请稍后再试");
        }
        EmailWindowCounter counter = EMAIL_IP_WINDOW_COUNTER.computeIfAbsent(clientIp, k -> new EmailWindowCounter(now, 0));
        synchronized (counter) {
            if (now - counter.windowStartMs > EMAIL_IP_WINDOW_MS) {
                counter.windowStartMs = now;
                counter.count = 0;
            }
            if (counter.count >= EMAIL_IP_MAX_PER_WINDOW) {
                throw new BusinessException(ResultCode.USER_REGISTER_RATE_LIMITED, "请求次数过多，请稍后再试");
            }
            counter.count += 1;
        }
        EMAIL_IP_LAST_APPLY_AT.put(clientIp, now);
    }

    private UserRegistrationResult findEmailAccount(String email) {
        LambdaQueryWrapper<UserRegistrationResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRegistrationResult::getAccountType, ACCOUNT_TYPE_EMAIL)
            .eq(UserRegistrationResult::getAccount, email)
            .eq(UserRegistrationResult::getStatus, 1)
            .orderByDesc(UserRegistrationResult::getId)
            .last("limit 1");
        return userRegistrationResultMapper.selectOne(wrapper);
    }

    private UserRegistrationResult insertEmailAccount(String email, JSONObject resultExt) {
        for (int i = 0; i < 5; i++) {
            UserRegistrationResult result = new UserRegistrationResult();
            result.setUserId(new BigDecimal(generateCrawlerResistantUserId()));
            result.setMergedToUserId(null);
            result.setAccountType(ACCOUNT_TYPE_EMAIL);
            result.setAccount(email);
            result.setStatus(1);
            result.setExtInfo(JSON.toJSONString(resultExt == null ? new JSONObject() : resultExt));
            try {
                userRegistrationResultMapper.insert(result);
                return result;
            } catch (DuplicateKeyException e) {
                UserRegistrationResult raced = findEmailAccount(email);
                if (raced != null) {
                    return raced;
                }
            }
        }
        throw new BusinessException(ResultCode.USER_REGISTER_APPLY_FAILED, "注册失败，请重试");
    }

    private UserRegistrationResult findOrCreateEmailAccount(String email, String clientIp) {
        UserRegistrationResult existing = findEmailAccount(email);
        if (existing != null) {
            return existing;
        }
        JSONObject resultExt = new JSONObject();
        resultExt.put("registerIp", clientIp == null ? "" : clientIp);
        resultExt.put("registerAt", LocalDateTime.now().toString());
        resultExt.put("registerChannel", "EMAIL_OTP");
        return insertEmailAccount(email, resultExt);
    }

    private AuthLoginResponse issueEmailSession(UserRegistrationResult account) {
        if (account == null || account.getUserId() == null) {
            throw new BusinessException(ResultCode.USER_REGISTER_PARAM_INVALID, "用户不存在");
        }
        String email = account.getAccount();
        AuthUserInfoResponse user = new AuthUserInfoResponse();
        user.setId(account.getUserId().toPlainString());
        user.setUsername(email);
        String nickname = email != null && email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        user.setNickname(nickname == null || nickname.isEmpty() ? "邮箱用户" : nickname);
        user.setAvatar("");

        String token = "Bearer " + IdUtil.fastSimpleUUID();
        userAuthSessionService.issue(token, user);
        AuthLoginResponse response = new AuthLoginResponse();
        response.setToken(token);
        response.setUser(user);
        return response;
    }

    private static class EmailWindowCounter {
        long windowStartMs;
        int count;

        EmailWindowCounter(long windowStartMs, int count) {
            this.windowStartMs = windowStartMs;
            this.count = count;
        }
    }
}
