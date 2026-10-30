package com.arelore.server.admin.controller;

import com.arelore.server.core.biz.prompt.user.UserAiPromptDailyVisitService;
import com.arelore.server.core.biz.prompt.user.UserAiPromptPrefService;
import com.arelore.server.core.biz.prompt.user.UserAiPromptService;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptDailyVisitRequest;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptDailyVisitResponse;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptPrefRequest;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptPrefResponse;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptRequest;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptResponse;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptVisitStatsResponse;
import com.arelore.server.core.biz.word.user.support.WordUserIdStrSupport;
import com.arelore.server.core.common.dto.PageResult;
import com.arelore.server.core.common.result.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/prompt")
public class AiPromptManageController {

    private final UserAiPromptPrefService prefService;
    private final UserAiPromptDailyVisitService visitService;
    private final UserAiPromptService promptService;

    public AiPromptManageController(
        UserAiPromptPrefService prefService,
        UserAiPromptDailyVisitService visitService,
        UserAiPromptService promptService
    ) {
        this.prefService = prefService;
        this.visitService = visitService;
        this.promptService = promptService;
    }

    @PostMapping("/pref/page")
    public Result<PageResult<UserAiPromptPrefResponse>> prefPage(
        @RequestBody(required = false) UserAiPromptPrefRequest request
    ) {
        return Result.success(prefService.pageQuery(
            request == null ? new UserAiPromptPrefRequest() : request
        ));
    }

    @PostMapping("/pref/detail")
    public Result<UserAiPromptPrefResponse> prefDetail(
        @RequestBody UserAiPromptPrefRequest request
    ) {
        WordUserIdStrSupport.applyIdStrToUserId(request);
        if (request == null) {
            return Result.error("请求不能为空");
        }
        if (request.getUserId() != null) {
            return Result.success(prefService.getOrDefault(request.getUserId()));
        }
        if (request.getId() != null) {
            UserAiPromptPrefResponse row = prefService.getById(request.getId());
            if (row == null) {
                return Result.error("记录不存在");
            }
            return Result.success(row);
        }
        return Result.error("请传 id 或 idStr/userId");
    }

    @PostMapping("/visit/page")
    public Result<PageResult<UserAiPromptDailyVisitResponse>> visitPage(
        @RequestBody(required = false) UserAiPromptDailyVisitRequest request
    ) {
        return Result.success(visitService.pageQuery(
            request == null ? new UserAiPromptDailyVisitRequest() : request
        ));
    }

    @PostMapping("/visit/stats")
    public Result<List<UserAiPromptVisitStatsResponse>> visitStats(
        @RequestBody(required = false) UserAiPromptDailyVisitRequest request
    ) {
        return Result.success(visitService.statsByDay(
            request == null ? new UserAiPromptDailyVisitRequest() : request
        ));
    }

    /** 管理端按用户查看其提示词列表 */
    @PostMapping("/user-prompt/page")
    public Result<PageResult<UserAiPromptResponse>> userPromptPage(
        @RequestBody UserAiPromptRequest request
    ) {
        WordUserIdStrSupport.applyIdStrToUserId(request);
        if (request == null || request.getUserId() == null) {
            return Result.error("请传 idStr（用户业务ID）");
        }
        return Result.success(promptService.pageQuery(request));
    }
}
