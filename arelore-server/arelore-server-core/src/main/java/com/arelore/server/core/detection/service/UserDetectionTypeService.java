package com.arelore.server.core.detection.service;

import com.arelore.server.core.common.service.BaseService;
import com.arelore.server.core.detection.dto.UserDetectionTypeRequest;
import com.arelore.server.core.detection.dto.UserDetectionTypeResponse;

import java.util.List;

public interface UserDetectionTypeService extends BaseService<UserDetectionTypeRequest, UserDetectionTypeResponse> {
    List<UserDetectionTypeResponse> listAll();
}
