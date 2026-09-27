package com.arelore.server.core.service.cert.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arelore.server.core.service.BaseServiceImpl;
import com.arelore.server.core.detection.dto.UserDetectionTypeRequest;
import com.arelore.server.core.detection.dto.UserDetectionTypeResponse;
import com.arelore.server.core.detection.entity.UserDetectionType;
import com.arelore.server.core.detection.mapper.UserDetectionTypeMapper;
import com.arelore.server.core.service.cert.admin.UserDetectionTypeService;
import com.arelore.server.core.detection.support.DetectionOperatorContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetectionTypeServiceImpl
    extends BaseServiceImpl<UserDetectionTypeRequest, UserDetectionTypeResponse, UserDetectionType>
    implements UserDetectionTypeService {
    private final UserDetectionTypeMapper mapper;

    public UserDetectionTypeServiceImpl(UserDetectionTypeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected UserDetectionTypeMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<UserDetectionTypeResponse> responseClass() {
        return UserDetectionTypeResponse.class;
    }

    @Override
    protected LambdaQueryWrapper<UserDetectionType> buildWrapper(UserDetectionTypeRequest request) {
        LambdaQueryWrapper<UserDetectionType> wrapper = new LambdaQueryWrapper<>();
        if (request == null) {
            return wrapper;
        }
        if (StringUtils.hasText(request.getTypeCode())) {
            wrapper.like(UserDetectionType::getTypeCode, request.getTypeCode());
        }
        if (StringUtils.hasText(request.getTypeName())) {
            wrapper.like(UserDetectionType::getTypeName, request.getTypeName());
        }
        wrapper.orderByDesc(UserDetectionType::getId);
        return wrapper;
    }

    @Override
    public List<UserDetectionTypeResponse> listAll() {
        UserDetectionTypeRequest request = new UserDetectionTypeRequest();
        return list(request);
    }

    @Override
    public List<UserDetectionTypeResponse> listByTypeCodes(Collection<String> typeCodes) {
        if (typeCodes == null || typeCodes.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> distinct = typeCodes.stream()
            .filter(StringUtils::hasText)
            .distinct()
            .collect(Collectors.toList());
        if (distinct.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<UserDetectionType> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserDetectionType::getTypeCode, distinct);
        return toResponses(mapper.selectList(wrapper));
    }

    @Override
    public int create(UserDetectionTypeRequest request) {
        request.setModifier(DetectionOperatorContext.getCurrentOperator());
        return super.create(request);
    }

    @Override
    public int update(UserDetectionTypeRequest request) {
        request.setModifier(DetectionOperatorContext.getCurrentOperator());
        return super.update(request);
    }

    @Override
    public int deleteById(Long id) {
        String operator = DetectionOperatorContext.getCurrentOperator();
        UserDetectionType exists = mapper.selectById(id);
        if (exists != null) {
            exists.setModifier(operator);
            mapper.updateById(exists);
        }
        return super.deleteById(id);
    }
}
