package com.arelore.server.core.service.cert.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.arelore.server.core.service.BaseServiceImpl;
import com.arelore.server.core.detection.dto.DetectionQuestionFavoriteToggleRequest;
import com.arelore.server.core.detection.dto.UserDetectionQuestionFavoriteRequest;
import com.arelore.server.core.detection.dto.UserDetectionQuestionFavoriteResponse;
import com.arelore.server.core.detection.entity.UserDetectionQuestionFavorite;
import com.arelore.server.core.detection.mapper.UserDetectionQuestionFavoriteMapper;
import com.arelore.server.core.service.cert.user.UserDetectionQuestionFavoriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetectionQuestionFavoriteServiceImpl
    extends BaseServiceImpl<UserDetectionQuestionFavoriteRequest, UserDetectionQuestionFavoriteResponse, UserDetectionQuestionFavorite>
    implements UserDetectionQuestionFavoriteService {
    private final UserDetectionQuestionFavoriteMapper mapper;

    public UserDetectionQuestionFavoriteServiceImpl(UserDetectionQuestionFavoriteMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected UserDetectionQuestionFavoriteMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<UserDetectionQuestionFavoriteResponse> responseClass() {
        return UserDetectionQuestionFavoriteResponse.class;
    }

    @Override
    protected LambdaQueryWrapper<UserDetectionQuestionFavorite> buildWrapper(UserDetectionQuestionFavoriteRequest request) {
        LambdaQueryWrapper<UserDetectionQuestionFavorite> wrapper = new LambdaQueryWrapper<>();
        if (request == null) {
            return wrapper;
        }
        if (StringUtils.hasText(request.getUserId())) {
            wrapper.eq(UserDetectionQuestionFavorite::getUserId, request.getUserId());
        }
        if (StringUtils.hasText(request.getQuestionTypeCode())) {
            wrapper.eq(UserDetectionQuestionFavorite::getQuestionTypeCode, request.getQuestionTypeCode());
        }
        if (StringUtils.hasText(request.getQuestionCode())) {
            wrapper.eq(UserDetectionQuestionFavorite::getQuestionCode, request.getQuestionCode());
        }
        wrapper.orderByDesc(UserDetectionQuestionFavorite::getId);
        return wrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFavorite(DetectionQuestionFavoriteToggleRequest request) {
        if (request == null
            || !StringUtils.hasText(request.getUserId())
            || !StringUtils.hasText(request.getQuestionTypeCode())
            || !StringUtils.hasText(request.getQuestionCode())) {
            throw new IllegalArgumentException("收藏参数不完整");
        }
        boolean targetFavorite = Boolean.TRUE.equals(request.getFavorite());
        LambdaQueryWrapper<UserDetectionQuestionFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDetectionQuestionFavorite::getUserId, request.getUserId())
            .eq(UserDetectionQuestionFavorite::getQuestionTypeCode, request.getQuestionTypeCode())
            .eq(UserDetectionQuestionFavorite::getQuestionCode, request.getQuestionCode())
            .last("limit 1");
        UserDetectionQuestionFavorite exists = mapper.selectOne(wrapper);

        if (targetFavorite) {
            if (exists == null) {
                UserDetectionQuestionFavorite favorite = new UserDetectionQuestionFavorite();
                favorite.setUserId(request.getUserId());
                favorite.setQuestionTypeCode(request.getQuestionTypeCode());
                favorite.setQuestionCode(request.getQuestionCode());
                favorite.setExtraInfo(request.getExtraInfo());
                mapper.insert(favorite);
            } else if (StringUtils.hasText(request.getExtraInfo())) {
                exists.setExtraInfo(request.getExtraInfo());
                mapper.updateById(exists);
            }
            return true;
        }

        if (exists != null) {
            mapper.deleteById(exists.getId());
        }
        return false;
    }

    @Override
    public List<String> listFavoriteQuestionCodes(String userId, String questionTypeCode) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(questionTypeCode)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<UserDetectionQuestionFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDetectionQuestionFavorite::getUserId, userId)
            .eq(UserDetectionQuestionFavorite::getQuestionTypeCode, questionTypeCode)
            .orderByDesc(UserDetectionQuestionFavorite::getId);
        return mapper.selectList(wrapper).stream()
            .map(UserDetectionQuestionFavorite::getQuestionCode)
            .filter(StringUtils::hasText)
            .distinct()
            .collect(Collectors.toList());
    }

    @Override
    public List<UserDetectionQuestionFavoriteResponse> listFavorites(String userId) {
        if (!StringUtils.hasText(userId)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<UserDetectionQuestionFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDetectionQuestionFavorite::getUserId, userId)
            .orderByDesc(UserDetectionQuestionFavorite::getId);
        return toResponses(mapper.selectList(wrapper));
    }

    @Override
    public IPage<UserDetectionQuestionFavorite> pageFavorites(String userId, int pageNum, int pageSize) {
        Page<UserDetectionQuestionFavorite> mpPage = new Page<>(Math.max(pageNum, 1), Math.max(pageSize, 1));
        if (!StringUtils.hasText(userId)) {
            return mpPage;
        }
        LambdaQueryWrapper<UserDetectionQuestionFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDetectionQuestionFavorite::getUserId, userId)
            .orderByDesc(UserDetectionQuestionFavorite::getId);
        return mapper.selectPage(mpPage, wrapper);
    }
}
