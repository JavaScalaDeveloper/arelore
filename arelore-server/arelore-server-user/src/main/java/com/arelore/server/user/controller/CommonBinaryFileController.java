package com.arelore.server.user.controller;

import com.arelore.server.core.common.result.Result;
import com.arelore.server.core.user.dto.CommonBinaryFileByHashRequest;
import com.arelore.server.core.user.dto.CommonBinaryFileByHashResponse;
import com.arelore.server.user.service.CommonBinaryFileQueryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/common-binary-file")
public class CommonBinaryFileController {
    private final CommonBinaryFileQueryService commonBinaryFileQueryService;

    public CommonBinaryFileController(CommonBinaryFileQueryService commonBinaryFileQueryService) {
        this.commonBinaryFileQueryService = commonBinaryFileQueryService;
    }

    @PostMapping("/get-by-hash")
    public Result<CommonBinaryFileByHashResponse> getByHash(@RequestBody CommonBinaryFileByHashRequest request) {
        if (request == null || request.getHashValue() == null || request.getHashValue().trim().isEmpty()) {
            return Result.error("hashValue 不能为空");
        }
        CommonBinaryFileByHashResponse response = commonBinaryFileQueryService.getByHash(request.getHashValue());
        if (response == null) {
            return Result.error(404, "图片不存在");
        }
        return Result.success(response);
    }
}

