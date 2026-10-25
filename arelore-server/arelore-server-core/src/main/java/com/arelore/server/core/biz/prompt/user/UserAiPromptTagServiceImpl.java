package com.arelore.server.core.biz.prompt.user;

import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptTagRequest;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptTagResponse;
import com.arelore.server.core.biz.prompt.user.entity.UserAiPromptTag;
import com.arelore.server.core.biz.prompt.user.mapper.UserAiPromptTagMapper;
import com.arelore.server.core.biz.word.user.support.WordUserIdStrSupport;
import com.arelore.server.core.service.BaseServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Locale;

@Service
public class UserAiPromptTagServiceImpl
    extends BaseServiceImpl<UserAiPromptTagRequest, UserAiPromptTagResponse, UserAiPromptTag>
    implements UserAiPromptTagService {

    public static final String STATUS_NORMAL = "NORMAL";
    public static final String STATUS_DISABLED = "DISABLED";
    public static final String STATUS_DELETED = "DELETED";
    public static final String TYPE_CONFIG = "CONFIG";
    public static final String TYPE_MANUAL = "MANUAL";

    private final UserAiPromptTagMapper mapper;

    public UserAiPromptTagServiceImpl(UserAiPromptTagMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected UserAiPromptTagMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<UserAiPromptTagResponse> responseClass() {
        return UserAiPromptTagResponse.class;
    }

    @Override
    protected UserAiPromptTagResponse toResponse(UserAiPromptTag entity) {
        UserAiPromptTagResponse response = super.toResponse(entity);
        WordUserIdStrSupport.fillIdStr(response);
        return response;
    }

    @Override
    protected LambdaQueryWrapper<UserAiPromptTag> buildWrapper(UserAiPromptTagRequest request) {
        WordUserIdStrSupport.applyIdStrToUserId(request);
        LambdaQueryWrapper<UserAiPromptTag> wrapper = new LambdaQueryWrapper<>();
        if (request == null) {
            return wrapper;
        }
        if (request.getUserId() != null) {
            wrapper.eq(UserAiPromptTag::getUserId, request.getUserId());
        }
        if (StringUtils.hasText(request.getTagType())) {
            wrapper.eq(UserAiPromptTag::getTagType, normalizeTagType(request.getTagType()));
        }
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(UserAiPromptTag::getStatus, request.getStatus().trim());
        } else {
            wrapper.ne(UserAiPromptTag::getStatus, STATUS_DELETED);
        }
        if (StringUtils.hasText(request.getName())) {
            wrapper.like(UserAiPromptTag::getName, request.getName().trim());
        }
        wrapper.orderByAsc(UserAiPromptTag::getTagType)
            .orderByAsc(UserAiPromptTag::getSortOrder)
            .orderByDesc(UserAiPromptTag::getId);
        return wrapper;
    }

    @Override
    public UserAiPromptTagResponse saveTag(UserAiPromptTagRequest request) {
        if (request == null || request.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("标签名不能为空");
        }
        String name = request.getName().trim();
        if (name.length() > 64) {
            throw new IllegalArgumentException("标签名最长64字符");
        }
        request.setName(name);
        request.setTagType(normalizeTagType(
            StringUtils.hasText(request.getTagType()) ? request.getTagType() : TYPE_MANUAL
        ));
        if (request.getColor() == null) {
            request.setColor("");
        }
        if (request.getSortOrder() == null) {
            request.setSortOrder(0);
        }
        if (!StringUtils.hasText(request.getStatus())) {
            request.setStatus(STATUS_NORMAL);
        } else {
            request.setStatus(normalizeStatus(request.getStatus()));
        }

        UserAiPromptTag exists = findByUserTypeAndName(
            request.getUserId(), request.getTagType(), name
        );
        if (exists == null) {
            create(request);
            exists = findByUserTypeAndName(request.getUserId(), request.getTagType(), name);
        } else {
            request.setId(exists.getId());
            update(request);
            exists = mapper.selectById(exists.getId());
        }
        return toResponse(exists);
    }

    @Override
    public int softDelete(BigDecimal userId, Long id) {
        if (userId == null || id == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        UserAiPromptTag entity = mapper.selectById(id);
        if (entity == null || entity.getUserId() == null || entity.getUserId().compareTo(userId) != 0) {
            throw new IllegalArgumentException("标签不存在");
        }
        entity.setStatus(STATUS_DELETED);
        return mapper.updateById(entity);
    }

    @Override
    public void ensureTags(BigDecimal userId, String tagType, Collection<String> names) {
        if (userId == null || CollectionUtils.isEmpty(names)) {
            return;
        }
        String type = normalizeTagType(StringUtils.hasText(tagType) ? tagType : TYPE_MANUAL);
        for (String raw : names) {
            if (!StringUtils.hasText(raw)) {
                continue;
            }
            String name = raw.trim();
            if (name.length() > 64) {
                continue;
            }
            UserAiPromptTag exists = findByUserTypeAndName(userId, type, name);
            if (exists != null) {
                if (STATUS_DELETED.equals(exists.getStatus())) {
                    exists.setStatus(STATUS_NORMAL);
                    mapper.updateById(exists);
                }
                continue;
            }
            UserAiPromptTagRequest req = new UserAiPromptTagRequest();
            req.setUserId(userId);
            req.setTagType(type);
            req.setName(name);
            req.setColor("");
            req.setSortOrder(0);
            req.setStatus(STATUS_NORMAL);
            create(req);
        }
    }

    private UserAiPromptTag findByUserTypeAndName(BigDecimal userId, String tagType, String name) {
        LambdaQueryWrapper<UserAiPromptTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAiPromptTag::getUserId, userId)
            .eq(UserAiPromptTag::getTagType, tagType)
            .eq(UserAiPromptTag::getName, name)
            .last("limit 1");
        return mapper.selectOne(wrapper);
    }

    private static String normalizeTagType(String raw) {
        String type = raw.trim().toUpperCase(Locale.ROOT);
        if (TYPE_CONFIG.equals(type) || TYPE_MANUAL.equals(type)) {
            return type;
        }
        throw new IllegalArgumentException("不支持的标签类型: " + raw + "（仅 CONFIG/MANUAL）");
    }

    private static String normalizeStatus(String raw) {
        String status = raw.trim().toUpperCase(Locale.ROOT);
        if (STATUS_NORMAL.equals(status) || STATUS_DISABLED.equals(status) || STATUS_DELETED.equals(status)) {
            return status;
        }
        throw new IllegalArgumentException("不支持的标签状态: " + raw);
    }
}
