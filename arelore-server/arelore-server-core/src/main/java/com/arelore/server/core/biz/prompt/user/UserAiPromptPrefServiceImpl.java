package com.arelore.server.core.biz.prompt.user;

import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptPrefRequest;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptPrefResponse;
import com.arelore.server.core.biz.prompt.user.entity.UserAiPromptPref;
import com.arelore.server.core.biz.prompt.user.mapper.UserAiPromptPrefMapper;
import com.arelore.server.core.biz.prompt.user.support.UserAiPromptPrefExtSupport;
import com.arelore.server.core.biz.word.user.support.WordUserIdStrSupport;
import com.arelore.server.core.service.BaseServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class UserAiPromptPrefServiceImpl
    extends BaseServiceImpl<UserAiPromptPrefRequest, UserAiPromptPrefResponse, UserAiPromptPref>
    implements UserAiPromptPrefService {

    private final UserAiPromptPrefMapper mapper;

    public UserAiPromptPrefServiceImpl(UserAiPromptPrefMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected UserAiPromptPrefMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<UserAiPromptPrefResponse> responseClass() {
        return UserAiPromptPrefResponse.class;
    }

    @Override
    protected LambdaQueryWrapper<UserAiPromptPref> buildWrapper(UserAiPromptPrefRequest request) {
        WordUserIdStrSupport.applyIdStrToUserId(request);
        LambdaQueryWrapper<UserAiPromptPref> wrapper = new LambdaQueryWrapper<>();
        if (request == null) {
            wrapper.ne(UserAiPromptPref::getStatus, UserAiPromptPrefExtSupport.STATUS_DELETED)
                .orderByDesc(UserAiPromptPref::getId);
            return wrapper;
        }
        if (request.getId() != null) {
            wrapper.eq(UserAiPromptPref::getId, request.getId());
        }
        if (request.getUserId() != null) {
            wrapper.eq(UserAiPromptPref::getUserId, request.getUserId());
        }
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(UserAiPromptPref::getStatus, request.getStatus().trim());
        } else {
            wrapper.ne(UserAiPromptPref::getStatus, UserAiPromptPrefExtSupport.STATUS_DELETED);
        }
        wrapper.orderByDesc(UserAiPromptPref::getModifyTime).orderByDesc(UserAiPromptPref::getId);
        return wrapper;
    }

    @Override
    protected UserAiPromptPrefResponse toResponse(UserAiPromptPref entity) {
        UserAiPromptPrefResponse response = super.toResponse(entity);
        if (response == null) {
            return null;
        }
        WordUserIdStrSupport.fillIdStr(response);
        UserAiPromptPrefExtSupport.fillVoFromExtJson(response);
        return response;
    }

    @Override
    public UserAiPromptPrefResponse getOrDefault(BigDecimal userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        UserAiPromptPref entity = findByUserId(userId);
        if (entity == null || UserAiPromptPrefExtSupport.STATUS_DELETED.equals(entity.getStatus())) {
            UserAiPromptPrefResponse defaults = new UserAiPromptPrefResponse();
            defaults.setUserId(userId);
            defaults.setIdStr(userId.toPlainString());
            defaults.setStatus(UserAiPromptPrefExtSupport.STATUS_NORMAL);
            defaults.setExtJson(UserAiPromptPrefExtSupport.defaultExtJson());
            UserAiPromptPrefExtSupport.fillVoFromExtJson(defaults);
            return defaults;
        }
        return toResponse(entity);
    }

    @Override
    public UserAiPromptPrefResponse savePref(UserAiPromptPrefRequest request) {
        WordUserIdStrSupport.applyIdStrToUserId(request);
        if (request == null || request.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        UserAiPromptPref exists = findByUserId(request.getUserId());
        String previousExt = exists == null ? null : exists.getExtJson();
        String extJson = UserAiPromptPrefExtSupport.buildExtJson(request, previousExt);
        LocalDateTime now = LocalDateTime.now();
        if (exists == null) {
            UserAiPromptPref entity = new UserAiPromptPref();
            entity.setUserId(request.getUserId());
            entity.setStatus(UserAiPromptPrefExtSupport.STATUS_NORMAL);
            entity.setExtJson(extJson);
            entity.setCreateTime(now);
            entity.setModifyTime(now);
            mapper.insert(entity);
            return toResponse(entity);
        }
        exists.setExtJson(extJson);
        exists.setStatus(UserAiPromptPrefExtSupport.STATUS_NORMAL);
        exists.setModifyTime(now);
        mapper.updateById(exists);
        return toResponse(exists);
    }

    private UserAiPromptPref findByUserId(BigDecimal userId) {
        LambdaQueryWrapper<UserAiPromptPref> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAiPromptPref::getUserId, userId).last("limit 1");
        return mapper.selectOne(wrapper);
    }
}
