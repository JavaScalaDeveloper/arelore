package com.arelore.server.core.registration.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arelore.server.core.registration.dto.MobileRegisterApplyRequest;
import com.arelore.server.core.registration.dto.MobileRegisterVerifyRequest;
import com.arelore.server.core.registration.entity.UserRegistrationApplication;
import com.arelore.server.core.registration.entity.UserRegistrationResult;
import com.arelore.server.core.registration.enums.AccountTypeEnum;
import com.arelore.server.core.registration.mapper.UserRegistrationApplicationMapper;
import com.arelore.server.core.registration.mapper.UserRegistrationResultMapper;
import com.arelore.server.core.registration.service.UserRegistrationService;
import com.arelore.server.core.registration.sms.AliyunSmsSender;
import com.arelore.server.core.registration.sms.SmsSender;
import com.arelore.server.core.registration.support.PasswordHashUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {
    // 账号类型统一从枚举获取，避免散落的魔法字符串。
    private static final String ACCOUNT_TYPE_MOBILE = AccountTypeEnum.MOBILE.code();
    private static final int CODE_VALID_MINUTES = 5;
    private static final long IP_COOLDOWN_MS = 60_000L; // 60s
    private static final long IP_WINDOW_MS = 60 * 60_000L; // 1h
    private static final int IP_MAX_PER_WINDOW = 5;

    private static final Map<String, Long> IP_LAST_APPLY_AT = new ConcurrentHashMap<>();
    private static final Map<String, WindowCounter> IP_WINDOW_COUNTER = new ConcurrentHashMap<>();

    private final UserRegistrationApplicationMapper applicationMapper;
    private final UserRegistrationResultMapper resultMapper;
    private final SmsSender smsSender;

    public UserRegistrationServiceImpl(
        UserRegistrationApplicationMapper applicationMapper,
        UserRegistrationResultMapper resultMapper,
        SmsSender smsSender
    ) {
        this.applicationMapper = applicationMapper;
        this.resultMapper = resultMapper;
        this.smsSender = smsSender;
    }

    @Override
    @Transactional(transactionManager = "userTransactionManager", rollbackFor = Exception.class)
    public void applyMobileRegister(MobileRegisterApplyRequest request, String clientIp) {
        // 1. 基础参数校验（手机号 + 密码）。
        if (request == null || !StringUtils.hasText(request.getMobile()) || !StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("手机号或密码不能为空");
        }
        String mobile = request.getMobile().trim();
        String password = request.getPassword();
        if (!mobile.matches("^1\\d{10}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("密码长度至少6位");
        }
        if (!StringUtils.hasText(clientIp) || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = "unknown";
        }

        // 2. 基于 IP 做注册申请限流，防止验证码接口被刷。
        enforceIpRateLimit(clientIp);

        // 3. 已注册手机号直接拒绝，避免重复注册。
        LambdaQueryWrapper<UserRegistrationResult> existsWrapper = new LambdaQueryWrapper<>();
        existsWrapper.eq(UserRegistrationResult::getAccountType, ACCOUNT_TYPE_MOBILE)
            .eq(UserRegistrationResult::getAccount, mobile);
        if (resultMapper.selectCount(existsWrapper) > 0) {
            throw new IllegalArgumentException("该手机号已注册");
        }

        String verifyCode = RandomUtil.randomNumbers(6);
        String salt = PasswordHashUtils.randomSaltBase64Url(16);
        String passwordHash = PasswordHashUtils.sha256(salt + ":" + password);
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(CODE_VALID_MINUTES);

        // 4. 将验证码、密码哈希、有效期写入申请表 ext_info，供后续验证码校验使用。
        JSONObject ext = new JSONObject();
        ext.put("passwordSalt", salt);
        ext.put("passwordHash", passwordHash);
        ext.put("verifyCode", verifyCode);
        ext.put("codeExpireAt", expireAt.toString());
        ext.put("codeValidMinutes", CODE_VALID_MINUTES);

        UserRegistrationApplication app = new UserRegistrationApplication();
        app.setAccountType(ACCOUNT_TYPE_MOBILE);
        app.setAccount(mobile);
        app.setClientIp(clientIp);
        app.setExtInfo(JSON.toJSONString(ext));
        applicationMapper.insert(app);

        // 5. 发送短信验证码。AliyunSmsSender 会读取 yml/env 配置并调用阿里云服务。
        int minutes = CODE_VALID_MINUTES;
        if (smsSender instanceof AliyunSmsSender aliyun) {
            minutes = aliyun.getDefaultValidMinutes();
        }
        smsSender.sendVerifyCode(mobile, verifyCode, minutes);
    }

    @Override
    @Transactional(transactionManager = "userTransactionManager", rollbackFor = Exception.class)
    public String verifyMobileRegister(MobileRegisterVerifyRequest request, String clientIp) {
        // 1. 验证基本参数。
        if (request == null || !StringUtils.hasText(request.getMobile()) || !StringUtils.hasText(request.getVerifyCode())) {
            throw new IllegalArgumentException("手机号或验证码不能为空");
        }
        String mobile = request.getMobile().trim();
        String code = request.getVerifyCode().trim();
        if (!mobile.matches("^1\\d{10}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        if (!code.matches("^\\d{4,8}$")) {
            throw new IllegalArgumentException("验证码格式不正确");
        }

        // 2. 读取该手机号最近一次注册申请并提取验证码信息。
        LambdaQueryWrapper<UserRegistrationApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRegistrationApplication::getAccountType, ACCOUNT_TYPE_MOBILE)
            .eq(UserRegistrationApplication::getAccount, mobile)
            .orderByDesc(UserRegistrationApplication::getId)
            .last("limit 1");
        UserRegistrationApplication latest = applicationMapper.selectOne(wrapper);
        if (latest == null || !StringUtils.hasText(latest.getExtInfo())) {
            throw new IllegalArgumentException("请先获取验证码");
        }

        JSONObject ext;
        try {
            ext = JSON.parseObject(latest.getExtInfo());
        } catch (Exception e) {
            throw new IllegalArgumentException("验证码信息异常，请重新获取");
        }

        // 3. 校验验证码与过期时间。
        String expected = ext == null ? "" : ext.getString("verifyCode");
        String expireAtStr = ext == null ? "" : ext.getString("codeExpireAt");
        if (!StringUtils.hasText(expected) || !StringUtils.hasText(expireAtStr)) {
            throw new IllegalArgumentException("验证码信息缺失，请重新获取");
        }
        if (!expected.equals(code)) {
            throw new IllegalArgumentException("验证码错误");
        }
        try {
            LocalDateTime expireAt = LocalDateTime.parse(expireAtStr);
            if (LocalDateTime.now().isAfter(expireAt)) {
                throw new IllegalArgumentException("验证码已过期，请重新获取");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("验证码过期时间异常，请重新获取");
        }

        // 4. 再次确认手机号未注册（兜底并发场景）。
        LambdaQueryWrapper<UserRegistrationResult> existsWrapper = new LambdaQueryWrapper<>();
        existsWrapper.eq(UserRegistrationResult::getAccountType, ACCOUNT_TYPE_MOBILE)
            .eq(UserRegistrationResult::getAccount, mobile);
        if (resultMapper.selectCount(existsWrapper) > 0) {
            throw new IllegalArgumentException("该手机号已注册");
        }

        // 生成反爬 user_id：以 2688 开头 + 16位不含4的数字（共20位）
        String passwordHash = ext == null ? "" : ext.getString("passwordHash");
        String passwordSalt = ext == null ? "" : ext.getString("passwordSalt");

        // 5. 将密码哈希与注册IP写入结果表 ext_info。
        JSONObject resultExt = new JSONObject();
        resultExt.put("passwordHash", passwordHash);
        resultExt.put("passwordSalt", passwordSalt);
        resultExt.put("registerIp", clientIp == null ? "" : clientIp);

        // 6. 生成带 2688 前缀的反爬 userId（尾号不含数字4），冲突则重试。
        for (int i = 0; i < 5; i++) {
            String userId = generateCrawlerResistantUserId();
            UserRegistrationResult result = new UserRegistrationResult();
            result.setUserId(new BigDecimal(userId));
            result.setMergedToUserId(null);
            result.setAccountType(ACCOUNT_TYPE_MOBILE);
            result.setAccount(mobile);
            result.setStatus(1);
            result.setExtInfo(JSON.toJSONString(resultExt));
            try {
                resultMapper.insert(result);
                return userId;
            } catch (DuplicateKeyException e) {
                // retry
            }
        }
        throw new IllegalStateException("注册失败，请重试");
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

    private void enforceIpRateLimit(String clientIp) {
        long now = System.currentTimeMillis();

        // 冷却时间：同一 IP 在冷却窗口内不允许再次申请。
        Long last = IP_LAST_APPLY_AT.get(clientIp);
        if (last != null && now - last < IP_COOLDOWN_MS) {
            throw new IllegalArgumentException("操作过于频繁，请稍后再试");
        }

        // 固定时间窗口限流：1小时最多 N 次。
        WindowCounter counter = IP_WINDOW_COUNTER.computeIfAbsent(clientIp, k -> new WindowCounter(now, 0));
        synchronized (counter) {
            if (now - counter.windowStartMs > IP_WINDOW_MS) {
                counter.windowStartMs = now;
                counter.count = 0;
            }
            if (counter.count >= IP_MAX_PER_WINDOW) {
                throw new IllegalArgumentException("请求次数过多，请稍后再试");
            }
            counter.count += 1;
        }
        IP_LAST_APPLY_AT.put(clientIp, now);
    }

    private static class WindowCounter {
        long windowStartMs;
        int count;

        WindowCounter(long windowStartMs, int count) {
            this.windowStartMs = windowStartMs;
            this.count = count;
        }
    }
}

