package com.arelore.server.admin.controller;

import com.arelore.server.core.biz.user.UserRegistrationResultService;
import com.arelore.server.core.biz.word.user.UserWordCurrentBookService;
import com.arelore.server.core.biz.word.user.UserWordLearnRecordService;
import com.arelore.server.core.biz.word.user.UserWordStudyPlanService;
import com.arelore.server.core.biz.word.user.dto.UserWordCurrentBookRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordCurrentBookResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordLearnRecordRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordLearnRecordResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyPlanRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyPlanResponse;
import com.arelore.server.core.common.dto.PageResult;
import com.arelore.server.core.common.result.Result;
import com.arelore.server.core.registration.dto.UserRegistrationResultRequest;
import com.arelore.server.core.registration.dto.UserRegistrationResultResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 背单词管理端：注册用户、词书进度、单词学习记录。
 */
@RestController
@RequestMapping("/api/admin/word/user")
public class WordUserManageController {
    private final UserRegistrationResultService registrationResultService;
    private final UserWordCurrentBookService currentBookService;
    private final UserWordStudyPlanService studyPlanService;
    private final UserWordLearnRecordService learnRecordService;

    public WordUserManageController(
        UserRegistrationResultService registrationResultService,
        UserWordCurrentBookService currentBookService,
        UserWordStudyPlanService studyPlanService,
        UserWordLearnRecordService learnRecordService
    ) {
        this.registrationResultService = registrationResultService;
        this.currentBookService = currentBookService;
        this.studyPlanService = studyPlanService;
        this.learnRecordService = learnRecordService;
    }

    /** 注册用户分页列表 */
    @PostMapping("/list")
    public Result<PageResult<UserRegistrationResultResponse>> userList(
        @RequestBody(required = false) UserRegistrationResultRequest request
    ) {
        return Result.success(
            registrationResultService.pageQuery(request == null ? new UserRegistrationResultRequest() : request)
        );
    }

    /** 用户当前词书进度（词书级） */
    @PostMapping("/current-book/list")
    public Result<PageResult<UserWordCurrentBookResponse>> currentBookList(
        @RequestBody(required = false) UserWordCurrentBookRequest request
    ) {
        return Result.success(
            currentBookService.pageQuery(request == null ? new UserWordCurrentBookRequest() : request)
        );
    }

    /** 用户学习计划（词书级） */
    @PostMapping("/study-plan/list")
    public Result<PageResult<UserWordStudyPlanResponse>> studyPlanList(
        @RequestBody(required = false) UserWordStudyPlanRequest request
    ) {
        return Result.success(
            studyPlanService.pageQuery(request == null ? new UserWordStudyPlanRequest() : request)
        );
    }

    /** 用户单词学习记录（单词级） */
    @PostMapping("/learn-record/list")
    public Result<PageResult<UserWordLearnRecordResponse>> learnRecordList(
        @RequestBody(required = false) UserWordLearnRecordRequest request
    ) {
        return Result.success(
            learnRecordService.pageQuery(request == null ? new UserWordLearnRecordRequest() : request)
        );
    }
}
