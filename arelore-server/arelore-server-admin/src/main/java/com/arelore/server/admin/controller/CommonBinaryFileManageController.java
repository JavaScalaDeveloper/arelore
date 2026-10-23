package com.arelore.server.admin.controller;

import com.arelore.server.core.biz.common.AdminCommonBinaryFileService;
import com.arelore.server.core.biz.common.dto.AdminCommonBinaryFileRequest;
import com.arelore.server.core.biz.common.dto.AdminCommonBinaryFileResponse;
import com.arelore.server.core.common.dto.IdRequest;
import com.arelore.server.core.common.dto.IdsRequest;
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

    /** 单条物理删除 */
    @PostMapping("/delete")
    public Result<String> delete(@RequestBody IdRequest request) {
        Long id = request == null ? null : request.getId();
        if (id == null) {
            return Result.error("ID 不能为空");
        }
        int rows = service.deleteById(id);
        if (rows <= 0) {
            return Result.error("记录不存在或已删除");
        }
        return Result.success("删除成功");
    }

    /** 批量物理删除（多选） */
    @PostMapping("/batch-delete")
    public Result<String> batchDelete(@RequestBody IdsRequest request) {
        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            return Result.error("请选择要删除的记录");
        }
        int rows = service.deleteByIds(request.getIds());
        return Result.success("已删除 " + rows + " 条");
    }
}
