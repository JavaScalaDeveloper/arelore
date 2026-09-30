package com.arelore.server.core.biz.user;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arelore.server.core.biz.user.dto.AuthUserInfoResponse;
import com.arelore.server.core.biz.user.dto.UserAuthSessionRequest;
import com.arelore.server.core.biz.user.dto.UserAuthSessionResponse;
import com.arelore.server.core.biz.user.entity.UserAuthSession;
import com.arelore.server.core.biz.user.mapper.UserAuthSessionMapper;
import com.arelore.server.core.service.BaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话持久化到 MySQL；进程内 ConcurrentHashMap 作本地缓存，后续可替换为 Redis。
 */
@Slf4j
@Service
public class UserAuthSessionServiceImpl
    extends BaseServiceImpl<UserAuthSessionRequest, UserAuthSessionResponse, UserAuthSession>
    implements UserAuthSessionService {

    private final UserAuthSessionMapper mapper;

    /** token -> 缓存条目（含过期时间） */
    private final ConcurrentHashMap<String, CachedSession> localCache = new ConcurrentHashMap<>();

    @Value("${auth.session.expire-days:30}")
    private int expireDays;

    public UserAuthSessionServiceImpl(UserAuthSessionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected UserAuthSessionMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<UserAuthSessionResponse> responseClass() {
        return UserAuthSessionResponse.class;
    }

    @Override
    protected LambdaQueryWrapper<UserAuthSession> buildWrapper(UserAuthSessionRequest request) {
        LambdaQueryWrapper<UserAuthSession> wrapper = new LambdaQueryWrapper<>();
        if (request == null) {
            return wrapper;
        }
        if (StringUtils.hasText(request.getToken())) {
            wrapper.eq(UserAuthSession::getToken, request.getToken());
        }
        wrapper.orderByDesc(UserAuthSession::getId);
        return wrapper;
    }

    @Override
    public void issue(String token, AuthUserInfoResponse user) {
        if (!StringUtils.hasText(token) || user == null || !StringUtils.hasText(user.getId())) {
            throw new IllegalArgumentException("token 或用户信息不能为空");
        }
        int days = expireDays <= 0 ? 30 : expireDays;
        LocalDateTime expireTime = LocalDateTime.now().plusDays(days);

        UserAuthSession session = new UserAuthSession();
        session.setToken(token);
        session.setExpireTime(expireTime);
        session.setUserInfo(JSON.toJSONString(user));
        mapper.insert(session);

        localCache.put(token, new CachedSession(copyUser(user), expireTime));
        log.info("签发登录会话成功，userId={}, expireDays={}", user.getId(), days);
    }

    @Override
    public AuthUserInfoResponse getValidUser(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();

        CachedSession cached = localCache.get(token);
        if (cached != null) {
            if (cached.expireTime != null && cached.expireTime.isAfter(now) && cached.user != null) {
                return copyUser(cached.user);
            }
            localCache.remove(token);
        }

        LambdaQueryWrapper<UserAuthSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAuthSession::getToken, token)
            .gt(UserAuthSession::getExpireTime, now)
            .last("limit 1");
        UserAuthSession session = mapper.selectOne(wrapper);
        if (session == null) {
            return null;
        }

        AuthUserInfoResponse user = parseUserInfo(session.getUserInfo());
        if (user == null) {
            return null;
        }
        localCache.put(token, new CachedSession(copyUser(user), session.getExpireTime()));
        return copyUser(user);
    }

    @Override
    public void invalidate(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }
        localCache.remove(token);
        LambdaQueryWrapper<UserAuthSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAuthSession::getToken, token);
        mapper.delete(wrapper);
    }

    private AuthUserInfoResponse parseUserInfo(String userInfo) {
        if (!StringUtils.hasText(userInfo)) {
            return null;
        }
        try {
            AuthUserInfoResponse user = JSON.parseObject(userInfo, AuthUserInfoResponse.class);
            if (user != null && StringUtils.hasText(user.getId())) {
                return user;
            }
        } catch (Exception e) {
            log.warn("解析会话 user_info 失败", e);
        }
        return null;
    }

    private static AuthUserInfoResponse copyUser(AuthUserInfoResponse src) {
        if (src == null) {
            return null;
        }
        AuthUserInfoResponse copy = new AuthUserInfoResponse();
        copy.setId(src.getId());
        copy.setUsername(src.getUsername());
        copy.setNickname(src.getNickname());
        copy.setAvatar(src.getAvatar());
        return copy;
    }

    private record CachedSession(AuthUserInfoResponse user, LocalDateTime expireTime) {
    }
}
