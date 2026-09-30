package com.arelore.server.core.biz.user;

import com.arelore.server.core.biz.user.dto.AuthUserInfoResponse;
import com.arelore.server.core.biz.user.dto.UserAuthSessionRequest;
import com.arelore.server.core.biz.user.dto.UserAuthSessionResponse;
import com.arelore.server.core.service.BaseService;

/**
 * 用户登录会话：MySQL 持久化 + 本地缓存（后续可替换为 Redis）。
 */
public interface UserAuthSessionService extends BaseService<UserAuthSessionRequest, UserAuthSessionResponse> {

    /**
     * 签发会话：写库并写入本地缓存。
     */
    void issue(String token, AuthUserInfoResponse user);

    /**
     * 校验并获取会话用户；优先本地缓存，未命中则查库并回填缓存。
     */
    AuthUserInfoResponse getValidUser(String token);

    /**
     * 失效会话：清缓存并删除库记录。
     */
    void invalidate(String token);
}
