package com.arelore.server.core.biz.common;

import com.arelore.server.core.biz.common.dto.AdminCommonBinaryFileRequest;
import com.arelore.server.core.biz.common.dto.AdminCommonBinaryFileResponse;
import com.arelore.server.core.service.BaseService;

import java.util.List;

public interface AdminCommonBinaryFileService
    extends BaseService<AdminCommonBinaryFileRequest, AdminCommonBinaryFileResponse> {

    /** 详情含文件内容（base64）；列表不含 */
    AdminCommonBinaryFileResponse detail(Long id);

    /** 按主键批量物理删除；返回成功删除行数 */
    int deleteByIds(List<Long> ids);
}
