package com.arelore.server.core.detection.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.arelore.server.core.detection.dto.DetectionTypeQueryRequest;
import com.arelore.server.core.detection.entity.UserDetectionType;

import java.util.List;

public interface UserDetectionTypeService {
    Page<UserDetectionType> pageQuery(DetectionTypeQueryRequest request);

    List<UserDetectionType> listAll();

    UserDetectionType getById(Long id);

    void create(UserDetectionType entity);

    void update(UserDetectionType entity);

    void delete(Long id);
}
