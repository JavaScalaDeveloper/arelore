package com.arelore.server.core.detection.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.arelore.server.core.detection.dto.DetectionQuestionQueryRequest;
import com.arelore.server.core.detection.entity.UserDetectionQuestion;

import java.util.List;

public interface UserDetectionQuestionService {
    Page<UserDetectionQuestion> pageQuery(DetectionQuestionQueryRequest request);

    List<UserDetectionQuestion> listByTypeCode(String typeCode);

    UserDetectionQuestion getById(Long id);

    void create(UserDetectionQuestion entity);

    void update(UserDetectionQuestion entity);

    void delete(Long id);
}
