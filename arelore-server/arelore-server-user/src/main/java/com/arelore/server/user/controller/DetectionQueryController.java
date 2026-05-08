package com.arelore.server.user.controller;

import com.arelore.server.core.common.result.Result;
import com.arelore.server.core.detection.dto.DetectionPaperListRequest;
import com.arelore.server.core.detection.dto.DetectionQuestionQueryRequest;
import com.arelore.server.core.detection.dto.DetectionQuestionFavoriteListRequest;
import com.arelore.server.core.detection.dto.DetectionQuestionFavoritePageRequest;
import com.arelore.server.core.detection.dto.DetectionQuestionFavoritePageResponse;
import com.arelore.server.core.detection.dto.DetectionQuestionFavoriteToggleRequest;
import com.arelore.server.core.detection.dto.DetectionResultQueryRequest;
import com.arelore.server.core.detection.dto.DetectionResultSaveRequest;
import com.arelore.server.core.detection.dto.DetectionResultSaveResponse;
import com.arelore.server.core.detection.dto.UserDetectionQuestionFavoriteResponse;
import com.arelore.server.core.detection.dto.UserDetectionQuestionRequest;
import com.arelore.server.core.detection.dto.UserDetectionQuestionResponse;
import com.arelore.server.core.detection.dto.UserDetectionTypeResponse;
import com.arelore.server.core.detection.dto.UserDetectResultHistoryResponse;
import com.arelore.server.core.detection.entity.UserDetectResult;
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
    public Result<List<UserDetectResultHistoryResponse>> historyResult(@RequestBody(required = false) DetectionResultQueryRequest request) {
        return Result.success(detectionQueryFacadeService.historyResult(request));
    }

    @PostMapping("/result/redo")
    public Result<Integer> redoResult(@RequestBody(required = false) DetectionResultQueryRequest request) {
        return Result.success(detectionQueryFacadeService.redoResult(request));
    }

    @PostMapping("/question/favorite/toggle")
    public Result<Boolean> toggleQuestionFavorite(@RequestBody DetectionQuestionFavoriteToggleRequest request) {
        return Result.success(detectionQueryFacadeService.toggleQuestionFavorite(request));
    }

    @PostMapping("/question/favorite/list")
    public Result<List<String>> listQuestionFavoriteCodes(@RequestBody(required = false) DetectionQuestionFavoriteListRequest request) {
        return Result.success(detectionQueryFacadeService.listQuestionFavoriteCodes(request));
    }

    /**
     * 考试记录（已交卷）：仅查询 history，且过滤 user_detect_result 为空的记录。
     */
    @PostMapping("/result/history/submitted")
    public Result<List<UserDetectResultHistoryResponse>> submittedHistory(@RequestBody(required = false) DetectionResultQueryRequest request) {
        return Result.success(detectionQueryFacadeService.listSubmittedHistory(request));
    }

    /**
     * 收藏夹：返回收藏明细列表。
     */
    @PostMapping("/question/favorite/list-detail")
    public Result<List<UserDetectionQuestionFavoriteResponse>> listFavorites(@RequestBody(required = false) DetectionResultQueryRequest request) {
        return Result.success(detectionQueryFacadeService.listFavorites(request));
    }

    /**
     * 收藏夹分页：分页查收藏记录后批量补全试卷名称与题干。
     */
    @PostMapping("/question/favorite/page")
    public Result<DetectionQuestionFavoritePageResponse> listFavoritesPage(@RequestBody(required = false) DetectionQuestionFavoritePageRequest request) {
        return Result.success(detectionQueryFacadeService.listFavoritesPage(request));
    }

    /**
     * 单题查询（只读）：需传 typeCode、questionCode。
     */
    @PostMapping("/question/one")
    public Result<UserDetectionQuestionResponse> questionOne(@RequestBody(required = false) DetectionQuestionQueryRequest request) {
        return Result.success(detectionQueryFacadeService.getQuestionOne(request));
    }
}
