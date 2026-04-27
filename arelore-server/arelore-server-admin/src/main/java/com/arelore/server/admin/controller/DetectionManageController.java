package com.arelore.server.admin.controller;

import com.arelore.server.core.common.dto.IdRequest;
import com.arelore.server.core.common.dto.PageResult;
import com.arelore.server.core.common.result.Result;
import com.arelore.server.core.detection.dto.UserDetectionQuestionRequest;
import com.arelore.server.core.detection.dto.UserDetectionQuestionResponse;
import com.arelore.server.core.detection.dto.UserDetectionTypeRequest;
import com.arelore.server.core.detection.dto.UserDetectionTypeResponse;
import com.arelore.server.core.detection.service.UserDetectionQuestionService;
import com.arelore.server.core.detection.service.UserDetectionTypeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/detection")
public class DetectionManageController {
    private final UserDetectionTypeService typeService;
    private final UserDetectionQuestionService questionService;

    public DetectionManageController(UserDetectionTypeService typeService, UserDetectionQuestionService questionService) {
        this.typeService = typeService;
        this.questionService = questionService;
    }

    @PostMapping("/type/list")
    public Result<PageResult<UserDetectionTypeResponse>> typeList(@RequestBody(required = false) UserDetectionTypeRequest request) {
        UserDetectionTypeRequest query = request == null ? new UserDetectionTypeRequest() : request;
        return Result.success(typeService.pageQuery(query));
    }

    @PostMapping("/type/create")
    public Result<String> typeCreate(@RequestBody UserDetectionTypeRequest request) {
        typeService.create(request);
        return Result.success("创建成功");
    }

    @PostMapping("/type/update")
    public Result<String> typeUpdate(@RequestBody UserDetectionTypeRequest request) {
        if (request.getId() == null) {
            return Result.error("ID 不能为空");
        }
        typeService.update(request);
        return Result.success("更新成功");
    }

    @PostMapping("/type/delete")
    public Result<String> typeDelete(@RequestBody IdRequest request) {
        Long id = request == null ? null : request.getId();
        if (id == null) {
            return Result.error("ID 不能为空");
        }
        typeService.deleteById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/question/list")
    public Result<PageResult<UserDetectionQuestionResponse>> questionList(@RequestBody(required = false) UserDetectionQuestionRequest request) {
        UserDetectionQuestionRequest query = request == null ? new UserDetectionQuestionRequest() : request;
        return Result.success(questionService.pageQuery(query));
    }

    @PostMapping("/question/create")
    public Result<String> questionCreate(@RequestBody UserDetectionQuestionRequest request) {
        questionService.create(request);
        return Result.success("创建成功");
    }

    @PostMapping("/question/update")
    public Result<String> questionUpdate(@RequestBody UserDetectionQuestionRequest request) {
        if (request.getId() == null) {
            return Result.error("ID 不能为空");
        }
        questionService.update(request);
        return Result.success("更新成功");
    }

    @PostMapping("/question/delete")
    public Result<String> questionDelete(@RequestBody IdRequest request) {
        Long id = request == null ? null : request.getId();
        if (id == null) {
            return Result.error("ID 不能为空");
        }
        questionService.deleteById(id);
        return Result.success("删除成功");
    }
}
