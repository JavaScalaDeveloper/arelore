package com.arelore.server.user.service;

import com.arelore.server.core.biz.user.dto.CommonBinaryFileByHashResponse;

public interface CommonBinaryFileQueryService {
    CommonBinaryFileByHashResponse getByHash(String hashValueHex);
}

