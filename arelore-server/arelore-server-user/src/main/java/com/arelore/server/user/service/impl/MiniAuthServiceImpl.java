package com.arelore.server.user.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arelore.server.core.common.exception.BusinessException;
import com.arelore.server.core.common.result.ResultCode;
import com.arelore.server.core.registration.entity.UserRegistrationApplication;
import com.arelore.server.core.registration.entity.UserRegistrationResult;
import com.arelore.server.core.registration.enums.AccountTypeEnum;
import com.arelore.server.core.registration.mapper.UserRegistrationApplicationMapper;
import com.arelore.server.core.registration.mapper.UserRegistrationResultMapper;
import com.arelore.server.core.registration.support.PasswordHashUtils;
import com.arelore.server.core.biz.user.dto.AuthLoginResponse;
import com.arelore.server.core.biz.user.dto.AuthUserInfoResponse;
import com.arelore.server.core.biz.user.dto.MiniManualLoginRequest;
import com.arelore.server.user.service.MiniAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 小程序认证服务：
 * - 手册登录：手机号 + 密码摘要（sha256）
 * - 登录流水写入 user_registration_application
 * - 账号信息写入/更新 user_registration_result.ext_info
 */
@Slf4j
@Service
public class MiniAuthServiceImpl implements MiniAuthService {

    private static final String ACCOUNT_TYPE_MOBILE = AccountTypeEnum.MOBILE.code();
    private static final String ACCOUNT_TYPE_MINI_MANUAL_LOGIN = AccountTypeEnum.MOBILE_MINI_MANUAL_LOGIN.code();

    private final UserRegistrationApplicationMapper applicationMapper;
    private final UserRegistrationResultMapper resultMapper;

    public MiniAuthServiceImpl(
        UserRegistrationApplicationMapper applicationMapper,
        UserRegistrationResultMapper resultMapper
    ) {
        this.applicationMapper = applicationMapper;
        this.resultMapper = resultMapper;
    }

    @Override
    @Transactional(transactionManager = "userTransactionManager", rollbackFor = Exception.class)
    public AuthLoginResponse manualLogin(MiniManualLoginRequest request, String clientIp) {
        if (request == null
            || !StringUtils.hasText(request.getMobile())
            || !StringUtils.hasText(request.getPasswordHash())) {
            throw new BusinessException(ResultCode.USER_REGISTER_PARAM_INVALID, "手机号或密码不能为空");
        }
        String mobile = request.getMobile().trim();
        String passwordHashCredential = request.getPasswordHash().trim();
        if (!mobile.matches("^1\\d{10}$")) {
            throw new BusinessException(ResultCode.USER_REGISTER_PARAM_INVALID, "手机号格式不正确");
        }
        if (!StringUtils.hasText(clientIp) || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = "unknown";
        }

        // 1) 写入登录流水到 application 表（新增一条）
        JSONObject appExt = new JSONObject();
        appExt.put("scene", "MINI_MANUAL_LOGIN");
        appExt.put("passwordHash", passwordHashCredential);
        appExt.put("loginAt", LocalDateTime.now().toString());
        UserRegistrationApplication app = new UserRegistrationApplication();
        app.setAccountType(ACCOUNT_TYPE_MINI_MANUAL_LOGIN);
        app.setAccount(mobile);
        app.setClientIp(clientIp);
        app.setExtInfo(JSON.toJSONString(appExt));
        applicationMapper.insert(app);

        // 2) 查找注册结果（不存在则自动创建，存在则校验密码）
        UserRegistrationResult result = findLatestMobileResult(mobile);
        if (result == null) {
            result = createMobileResult(mobile, passwordHashCredential, clientIp);
        } else {
            verifyPassword(result, passwordHashCredential);
            // 同步把最近登录信息写入 ext_info
            JSONObject ext = parseExtInfo(result.getExtInfo());
            ext.put("lastLoginAt", LocalDateTime.now().toString());
            ext.put("lastLoginIp", clientIp);
            result.setExtInfo(JSON.toJSONString(ext));
            resultMapper.updateById(result);
        }

        // 3) 返回 token + user
        String token = "Bearer " + IdUtil.fastSimpleUUID();
        AuthUserInfoResponse user = new AuthUserInfoResponse();
        user.setId(result.getUserId() == null ? "" : result.getUserId().toPlainString());
        user.setUsername(mobile);
        user.setNickname(mobile);
        user.setAvatar("");

        AuthLoginResponse resp = new AuthLoginResponse();
        resp.setToken(token);
        resp.setUser(user);
        return resp;
    }

    private UserRegistrationResult findLatestMobileResult(String mobile) {
        LambdaQueryWrapper<UserRegistrationResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRegistrationResult::getAccountType, ACCOUNT_TYPE_MOBILE)
            .eq(UserRegistrationResult::getAccount, mobile)
            .eq(UserRegistrationResult::getStatus, 1)
            .orderByDesc(UserRegistrationResult::getId)
            .last("limit 1");
        return resultMapper.selectOne(wrapper);
    }

    private UserRegistrationResult createMobileResult(String mobile, String passwordHashCredential, String clientIp) {
        String salt = PasswordHashUtils.randomSaltBase64Url(16);
        String passwordHash = PasswordHashUtils.sha256(salt + ":" + passwordHashCredential);

        JSONObject ext = new JSONObject();
        ext.put("passwordSalt", salt);
        ext.put("passwordHash", passwordHash);
        ext.put("registerIp", clientIp);
        ext.put("registerAt", LocalDateTime.now().toString());
        ext.put("lastLoginAt", LocalDateTime.now().toString());
        ext.put("lastLoginIp", clientIp);

        for (int i = 0; i < 5; i++) {
            String userId = generateCrawlerResistantUserId();
            UserRegistrationResult result = new UserRegistrationResult();
            result.setUserId(new BigDecimal(userId));
            result.setMergedToUserId(null);
            result.setAccountType(ACCOUNT_TYPE_MOBILE);
            result.setAccount(mobile);
            result.setStatus(1);
            result.setExtInfo(JSON.toJSONString(ext));
            try {
                resultMapper.insert(result);
                return result;
            } catch (DuplicateKeyException e) {
                // retry
            }
        }
        throw new BusinessException(ResultCode.BUSINESS_ERROR, "创建账号失败，请重试");
    }

    private void verifyPassword(UserRegistrationResult result, String credential) {
        JSONObject ext = parseExtInfo(result.getExtInfo());
        String salt = ext.getString("passwordSalt");
        String expectedHash = ext.getString("passwordHash");
        if (!StringUtils.hasText(salt) || !StringUtils.hasText(expectedHash)) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR, "密码错误");
        }
        String actualHash = PasswordHashUtils.sha256(salt + ":" + credential);
        if (!expectedHash.equals(actualHash) && !expectedHash.equals(credential)) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR, "密码错误");
        }
    }

    private JSONObject parseExtInfo(String extInfo) {
        if (!StringUtils.hasText(extInfo)) {
            return new JSONObject();
        }
        try {
            JSONObject parsed = JSON.parseObject(extInfo);
            return parsed == null ? new JSONObject() : parsed;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    /**
     * 生成 20 位 user_id：
     * - 固定前缀 2688
     * - 后 16 位随机数字，且每一位都不允许出现数字 4
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

