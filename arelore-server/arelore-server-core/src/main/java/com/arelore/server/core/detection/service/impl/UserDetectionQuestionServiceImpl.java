package com.arelore.server.core.detection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arelore.server.core.common.service.impl.BaseServiceImpl;
import com.arelore.server.core.detection.dto.UserDetectionQuestionRequest;
import com.arelore.server.core.detection.dto.UserDetectionQuestionResponse;
import com.arelore.server.core.detection.entity.UserDetectionQuestion;
import com.arelore.server.core.detection.mapper.UserDetectionQuestionMapper;
import com.arelore.server.core.detection.service.UserDetectionQuestionService;
import com.arelore.server.core.detection.support.DetectionOperatorContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserDetectionQuestionServiceImpl
    extends BaseServiceImpl<UserDetectionQuestionRequest, UserDetectionQuestionResponse, UserDetectionQuestion>
    implements UserDetectionQuestionService {
    private final UserDetectionQuestionMapper mapper;

    public UserDetectionQuestionServiceImpl(UserDetectionQuestionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected UserDetectionQuestionMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<UserDetectionQuestionResponse> responseClass() {
        return UserDetectionQuestionResponse.class;
    }

    @Override
    protected LambdaQueryWrapper<UserDetectionQuestion> buildWrapper(UserDetectionQuestionRequest request) {
        LambdaQueryWrapper<UserDetectionQuestion> wrapper = new LambdaQueryWrapper<>();
        if (request == null) {
            return wrapper;
        }
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
        return wrapper;
    }

    @Override
    public List<UserDetectionQuestionResponse> listByTypeCode(String typeCode) {
        UserDetectionQuestionRequest request = new UserDetectionQuestionRequest();
        request.setTypeCode(typeCode);
        return list(request);
    }

    @Override
    public UserDetectionQuestionResponse getByTypeAndQuestionCode(String typeCode, String questionCode) {
        if (!StringUtils.hasText(typeCode) || !StringUtils.hasText(questionCode)) {
            return null;
        }
        LambdaQueryWrapper<UserDetectionQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDetectionQuestion::getTypeCode, typeCode)
            .eq(UserDetectionQuestion::getQuestionCode, questionCode)
            .last("limit 1");
        return toResponse(mapper.selectOne(wrapper));
    }

    @Override
    public int create(UserDetectionQuestionRequest request) {
        request.setModifier(DetectionOperatorContext.getCurrentOperator());
        return super.create(request);
    }

    @Override
    public int update(UserDetectionQuestionRequest request) {
        request.setModifier(DetectionOperatorContext.getCurrentOperator());
        return super.update(request);
    }

    @Override
    public int deleteById(Long id) {
        String operator = DetectionOperatorContext.getCurrentOperator();
        UserDetectionQuestion exists = mapper.selectById(id);
        if (exists != null) {
            exists.setModifier(operator);
            mapper.updateById(exists);
        }
        return super.deleteById(id);
    }
}
