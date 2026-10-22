package com.arelore.server.core.biz.common;

import com.arelore.server.core.biz.common.dto.AdminCommonBinaryFileRequest;
import com.arelore.server.core.biz.common.dto.AdminCommonBinaryFileResponse;
import com.arelore.server.core.service.BaseService;

public interface AdminCommonBinaryFileService
    extends BaseService<AdminCommonBinaryFileRequest, AdminCommonBinaryFileResponse> {

    /** 详情含文件内容（base64）；列表不含 */
    AdminCommonBinaryFileResponse detail(Long id);
}
