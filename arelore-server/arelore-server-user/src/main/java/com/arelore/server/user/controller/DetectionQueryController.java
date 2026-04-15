package com.arelore.server.user.controller;

import com.arelore.server.common.result.Result;
import com.arelore.server.core.detection.dto.DetectionResultSaveRequest;
import com.arelore.server.core.detection.dto.DetectionResultSaveResponse;
import com.arelore.server.core.detection.entity.UserDetectResult;
import com.arelore.server.core.detection.entity.UserDetectResultHistory;
import com.arelore.server.core.detection.entity.UserDetectionQuestion;
import com.arelore.server.core.detection.entity.UserDetectionType;
import com.arelore.server.core.detection.service.UserDetectionQuestionService;
import com.arelore.server.core.detection.service.UserDetectionResultService;
import com.arelore.server.core.detection.service.UserDetectionTypeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/detection")
public class DetectionQueryController {
    private final UserDetectionTypeService typeService;
    private final UserDetectionQuestionService questionService;
    private final UserDetectionResultService resultService;

    public DetectionQueryController(
        UserDetectionTypeService typeService,
        UserDetectionQuestionService questionService,
        UserDetectionResultService resultService
    ) {
        this.typeService = typeService;
        this.questionService = questionService;
        this.resultService = resultService;
    }

    @PostMapping("/type/all")
    public Result<List<UserDetectionType>> typeAll() {
        return Result.success(typeService.listAll());
    }

    @PostMapping("/question/all")
    public Result<List<UserDetectionQuestion>> questionAll(@RequestBody(required = false) Map<String, String> request) {
        String typeCode = request == null ? null : request.get("typeCode");
        return Result.success(questionService.listByTypeCode(typeCode));
    }

    @PostMapping("/result/save")
    public Result<DetectionResultSaveResponse> saveResult(@RequestBody DetectionResultSaveRequest request) {
        return Result.success(resultService.saveResultAndHistory(request));
    }

    @PostMapping("/result/current")
    public Result<UserDetectResult> currentResult(@RequestBody Map<String, String> request) {
        String userId = request == null ? null : request.get("userId");
        String typeCode = request == null ? null : request.get("typeCode");
        return Result.success(resultService.getCurrentResult(userId, typeCode));
    }

    @PostMapping("/result/history")
    public Result<List<UserDetectResultHistory>> historyResult(@RequestBody Map<String, String> request) {
        String userId = request == null ? null : request.get("userId");
        String typeCode = request == null ? null : request.get("typeCode");
        return Result.success(resultService.listHistory(userId, typeCode));
    }
}
