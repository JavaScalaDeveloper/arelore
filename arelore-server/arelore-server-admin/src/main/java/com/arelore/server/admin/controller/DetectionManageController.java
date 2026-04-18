package com.arelore.server.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.arelore.server.core.common.dto.PageResult;
import com.arelore.server.core.common.result.Result;
import com.arelore.server.core.detection.dto.DetectionQuestionQueryRequest;
import com.arelore.server.core.detection.dto.DetectionTypeQueryRequest;
import com.arelore.server.core.detection.entity.UserDetectionQuestion;
import com.arelore.server.core.detection.entity.UserDetectionType;
import com.arelore.server.core.detection.service.UserDetectionQuestionService;
import com.arelore.server.core.detection.service.UserDetectionTypeService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public Result<PageResult<UserDetectionType>> typeList(@RequestBody(required = false) DetectionTypeQueryRequest request) {
        DetectionTypeQueryRequest query = request == null ? new DetectionTypeQueryRequest() : request;
        Page<UserDetectionType> page = typeService.pageQuery(query);
        return Result.success(PageResult.of(page.getRecords(), query.getPageNum(), query.getPageSize(), page.getTotal()));
    }

    @PostMapping("/type/create")
    public Result<String> typeCreate(@RequestBody UserDetectionType request) {
        typeService.create(request);
        return Result.success("创建成功");
    }

    @PostMapping("/type/update")
    public Result<String> typeUpdate(@RequestBody UserDetectionType request) {
        if (request.getId() == null) {
            return Result.error("ID 不能为空");
        }
        typeService.update(request);
        return Result.success("更新成功");
    }

    @PostMapping("/type/delete")
    public Result<String> typeDelete(@RequestBody Map<String, Long> request) {
        Long id = request.get("id");
        if (id == null) {
            return Result.error("ID 不能为空");
        }
        typeService.delete(id);
        return Result.success("删除成功");
    }

    @PostMapping("/question/list")
    public Result<PageResult<UserDetectionQuestion>> questionList(@RequestBody(required = false) DetectionQuestionQueryRequest request) {
        DetectionQuestionQueryRequest query = request == null ? new DetectionQuestionQueryRequest() : request;
        Page<UserDetectionQuestion> page = questionService.pageQuery(query);
        return Result.success(PageResult.of(page.getRecords(), query.getPageNum(), query.getPageSize(), page.getTotal()));
    }

    @PostMapping("/question/create")
    public Result<String> questionCreate(@RequestBody UserDetectionQuestion request) {
        questionService.create(request);
        return Result.success("创建成功");
    }

    @PostMapping("/question/update")
    public Result<String> questionUpdate(@RequestBody UserDetectionQuestion request) {
        if (request.getId() == null) {
            return Result.error("ID 不能为空");
        }
        questionService.update(request);
        return Result.success("更新成功");
    }

    @PostMapping("/question/delete")
    public Result<String> questionDelete(@RequestBody Map<String, Long> request) {
        Long id = request.get("id");
        if (id == null) {
            return Result.error("ID 不能为空");
        }
        questionService.delete(id);
        return Result.success("删除成功");
    }
}
