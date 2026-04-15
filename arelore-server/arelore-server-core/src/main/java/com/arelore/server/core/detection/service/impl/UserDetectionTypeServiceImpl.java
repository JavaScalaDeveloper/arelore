package com.arelore.server.core.detection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.arelore.server.core.detection.dto.DetectionTypeQueryRequest;
import com.arelore.server.core.detection.entity.UserDetectionType;
import com.arelore.server.core.detection.mapper.UserDetectionTypeMapper;
import com.arelore.server.core.detection.service.UserDetectionTypeService;
import com.arelore.server.core.detection.support.DetectionOperatorContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserDetectionTypeServiceImpl implements UserDetectionTypeService {
    private final UserDetectionTypeMapper mapper;

    public UserDetectionTypeServiceImpl(UserDetectionTypeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Page<UserDetectionType> pageQuery(DetectionTypeQueryRequest request) {
        Page<UserDetectionType> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<UserDetectionType> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getTypeCode())) {
            wrapper.like(UserDetectionType::getTypeCode, request.getTypeCode());
        }
        if (StringUtils.hasText(request.getTypeName())) {
            wrapper.like(UserDetectionType::getTypeName, request.getTypeName());
        }
        wrapper.orderByDesc(UserDetectionType::getId);
        return mapper.selectPage(page, wrapper);
    }

    @Override
    public List<UserDetectionType> listAll() {
        LambdaQueryWrapper<UserDetectionType> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(UserDetectionType::getId);
        return mapper.selectList(wrapper);
    }

    @Override
    public UserDetectionType getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public void create(UserDetectionType entity) {
        entity.setModifier(DetectionOperatorContext.getCurrentOperator());
        mapper.insert(entity);
    }

    @Override
    public void update(UserDetectionType entity) {
        entity.setModifier(DetectionOperatorContext.getCurrentOperator());
        mapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        String operator = DetectionOperatorContext.getCurrentOperator();
        UserDetectionType exists = mapper.selectById(id);
        if (exists != null) {
            exists.setModifier(operator);
            mapper.updateById(exists);
        }
        mapper.deleteById(id);
    }
}
