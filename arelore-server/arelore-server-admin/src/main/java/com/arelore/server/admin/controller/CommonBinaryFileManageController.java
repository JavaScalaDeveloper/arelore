package com.arelore.server.admin.controller;

import com.arelore.server.core.biz.common.AdminCommonBinaryFileService;
import com.arelore.server.core.biz.common.dto.AdminCommonBinaryFileRequest;
import com.arelore.server.core.biz.common.dto.AdminCommonBinaryFileResponse;
import com.arelore.server.core.common.dto.IdRequest;
import com.arelore.server.core.common.dto.PageResult;
import com.arelore.server.core.common.result.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/common/binary-file")
public class CommonBinaryFileManageController {
    private final AdminCommonBinaryFileService service;

    public CommonBinaryFileManageController(AdminCommonBinaryFileService service) {
        this.service = service;
    }

    /** 分页列表：不含文件二进制 */
    @PostMapping("/list")
    public Result<PageResult<AdminCommonBinaryFileResponse>> list(
        @RequestBody(required = false) AdminCommonBinaryFileRequest request
    ) {
        return Result.success(service.pageQuery(request == null ? new AdminCommonBinaryFileRequest() : request));
    }

    /** 详情：含 base64 文件内容，供预览 */
    @PostMapping("/detail")
    public Result<AdminCommonBinaryFileResponse> detail(@RequestBody IdRequest request) {
        Long id = request == null ? null : request.getId();
        if (id == null) {
            return Result.error("ID 不能为空");
        }
        try {
            return Result.success(service.detail(id));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
