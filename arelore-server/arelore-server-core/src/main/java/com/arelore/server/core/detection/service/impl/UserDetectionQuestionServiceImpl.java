package com.arelore.server.core.detection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.arelore.server.core.detection.dto.DetectionQuestionQueryRequest;
import com.arelore.server.core.detection.entity.UserDetectionQuestion;
import com.arelore.server.core.detection.mapper.UserDetectionQuestionMapper;
import com.arelore.server.core.detection.service.UserDetectionQuestionService;
import com.arelore.server.core.detection.support.DetectionOperatorContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserDetectionQuestionServiceImpl implements UserDetectionQuestionService {
    private final UserDetectionQuestionMapper mapper;

    public UserDetectionQuestionServiceImpl(UserDetectionQuestionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Page<UserDetectionQuestion> pageQuery(DetectionQuestionQueryRequest request) {
        Page<UserDetectionQuestion> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<UserDetectionQuestion> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getTypeCode())) {
            wrapper.eq(UserDetectionQuestion::getTypeCode, request.getTypeCode());
        }
        if (StringUtils.hasText(request.getQuestionCode())) {
            wrapper.like(UserDetectionQuestion::getQuestionCode, request.getQuestionCode());
        }
        if (StringUtils.hasText(request.getQuestionName())) {
            wrapper.like(UserDetectionQuestion::getQuestionName, request.getQuestionName());
        }
        wrapper.orderByAsc(UserDetectionQuestion::getQuestionOrder).orderByDesc(UserDetectionQuestion::getId);
        return mapper.selectPage(page, wrapper);
    }

    @Override
    public List<UserDetectionQuestion> listByTypeCode(String typeCode) {
        LambdaQueryWrapper<UserDetectionQuestion> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(typeCode)) {
            wrapper.eq(UserDetectionQuestion::getTypeCode, typeCode);
        }
        wrapper.orderByAsc(UserDetectionQuestion::getQuestionOrder).orderByDesc(UserDetectionQuestion::getId);
        return mapper.selectList(wrapper);
    }

    @Override
    public UserDetectionQuestion getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public void create(UserDetectionQuestion entity) {
        entity.setModifier(DetectionOperatorContext.getCurrentOperator());
        mapper.insert(entity);
    }

    @Override
    public void update(UserDetectionQuestion entity) {
        entity.setModifier(DetectionOperatorContext.getCurrentOperator());
        mapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        String operator = DetectionOperatorContext.getCurrentOperator();
        UserDetectionQuestion exists = mapper.selectById(id);
        if (exists != null) {
            exists.setModifier(operator);
            mapper.updateById(exists);
        }
        mapper.deleteById(id);
    }
}
