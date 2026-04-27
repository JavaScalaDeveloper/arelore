package com.arelore.server.user.controller;

import com.arelore.server.core.common.result.Result;
import com.arelore.server.core.detection.dto.DetectionPaperListRequest;
import com.arelore.server.core.detection.dto.DetectionResultQueryRequest;
import com.arelore.server.core.detection.dto.DetectionResultSaveRequest;
import com.arelore.server.core.detection.dto.DetectionResultSaveResponse;
import com.arelore.server.core.detection.dto.UserDetectionQuestionRequest;
import com.arelore.server.core.detection.dto.UserDetectionQuestionResponse;
import com.arelore.server.core.detection.dto.UserDetectionTypeResponse;
import com.arelore.server.core.detection.entity.UserDetectResult;
import com.arelore.server.core.detection.entity.UserDetectResultHistory;
import com.arelore.server.user.service.DetectionQueryFacadeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/detection")
public class DetectionQueryController {
    private final DetectionQueryFacadeService detectionQueryFacadeService;

    public DetectionQueryController(
        DetectionQueryFacadeService detectionQueryFacadeService
    ) {
        this.detectionQueryFacadeService = detectionQueryFacadeService;
    }

    @PostMapping("/type/all")
    public Result<List<UserDetectionTypeResponse>> typeAll() {
        return Result.success(detectionQueryFacadeService.listAllTypes());
    }

    @PostMapping("/question/all")
    public Result<List<UserDetectionQuestionResponse>> questionAll(@RequestBody(required = false) UserDetectionQuestionRequest request) {
        return Result.success(detectionQueryFacadeService.listQuestions(request));
    }

    /**
     * 查询试卷列表（用于小程序刷题菜单）。
     * 例如：examCategory=软考, subject=软件设计师。
     */
    @PostMapping("/paper/list")
    public Result<List<UserDetectionTypeResponse>> paperList(@RequestBody(required = false) DetectionPaperListRequest request) {
        return Result.success(detectionQueryFacadeService.listPapers(request));
    }

    @PostMapping("/result/save")
    public Result<DetectionResultSaveResponse> saveResult(@RequestBody DetectionResultSaveRequest request) {
        return Result.success(detectionQueryFacadeService.saveResult(request));
    }

    @PostMapping("/result/current")
    public Result<UserDetectResult> currentResult(@RequestBody(required = false) DetectionResultQueryRequest request) {
        return Result.success(detectionQueryFacadeService.currentResult(request));
    }

    @PostMapping("/result/history")
    public Result<List<UserDetectResultHistory>> historyResult(@RequestBody(required = false) DetectionResultQueryRequest request) {
        return Result.success(detectionQueryFacadeService.historyResult(request));
    }
}
