package com.arelore.server.admin.controller;

import com.arelore.server.core.biz.common.AdminCommonUserDailyVisitService;
import com.arelore.server.core.biz.common.dto.AdminCommonUserDailyVisitRequest;
import com.arelore.server.core.biz.common.dto.AdminCommonUserDailyVisitResponse;
import com.arelore.server.core.common.dto.IdRequest;
import com.arelore.server.core.common.dto.IdsRequest;
import com.arelore.server.core.common.dto.PageResult;
import com.arelore.server.core.common.result.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/common/user-daily-visit")
public class CommonUserDailyVisitManageController {
    private final AdminCommonUserDailyVisitService service;

    public CommonUserDailyVisitManageController(AdminCommonUserDailyVisitService service) {
        this.service = service;
    }

    @PostMapping("/list")
    public Result<PageResult<AdminCommonUserDailyVisitResponse>> list(
        @RequestBody(required = false) AdminCommonUserDailyVisitRequest request
    ) {
        return Result.success(service.pageQuery(request == null ? new AdminCommonUserDailyVisitRequest() : request));
    }

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

    @PostMapping("/batch-delete")
    public Result<String> batchDelete(@RequestBody IdsRequest request) {
        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            return Result.error("请选择要删除的记录");
        }
        int rows = service.deleteByIds(request.getIds());
        return Result.success("已删除 " + rows + " 条");
    }
}
