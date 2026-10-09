package com.arelore.server.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arelore.server.core.common.result.Result;
import com.arelore.server.core.registration.entity.UserRegistrationResult;
import com.arelore.server.core.registration.enums.AccountTypeEnum;
import com.arelore.server.core.registration.mapper.UserRegistrationResultMapper;
import com.arelore.server.core.biz.user.dto.AuthUserInfoResponse;
import com.arelore.server.core.biz.user.AuthService;
import com.arelore.server.core.biz.word.admin.AdminWordBookService;
import com.arelore.server.core.biz.word.admin.AdminWordCategoryService;
import com.arelore.server.core.biz.word.admin.AdminWordLanguageService;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBookRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBookResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordCategoryRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordCategoryResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordLanguageRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordLanguageResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordCurrentBookRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordCurrentBookResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyAnswerRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyAnswerResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyPlanRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyPlanResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordStudySessionRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordStudySessionResponse;
import com.arelore.server.core.biz.word.user.UserWordCurrentBookService;
import com.arelore.server.core.biz.word.user.UserWordLearnRecordService;
import com.arelore.server.core.biz.word.user.UserWordStudyPlanService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/user/word")
public class WordQueryController {
    private final AdminWordLanguageService languageService;
    private final AdminWordCategoryService categoryService;
    private final AdminWordBookService bookService;
    private final UserWordCurrentBookService currentBookService;
    private final UserWordStudyPlanService studyPlanService;
    private final UserWordLearnRecordService learnRecordService;
    private final AuthService authService;
    private final UserRegistrationResultMapper registrationResultMapper;

    public WordQueryController(
        AdminWordLanguageService languageService,
        AdminWordCategoryService categoryService,
        AdminWordBookService bookService,
        UserWordCurrentBookService currentBookService,
        UserWordStudyPlanService studyPlanService,
        UserWordLearnRecordService learnRecordService,
        AuthService authService,
        UserRegistrationResultMapper registrationResultMapper
    ) {
        this.languageService = languageService;
        this.categoryService = categoryService;
        this.bookService = bookService;
        this.currentBookService = currentBookService;
        this.studyPlanService = studyPlanService;
        this.learnRecordService = learnRecordService;
        this.authService = authService;
        this.registrationResultMapper = registrationResultMapper;
    }

    @PostMapping("/language/list")
    public Result<List<AdminWordLanguageResponse>> languageList(
        @RequestBody(required = false) AdminWordLanguageRequest request
    ) {
        if (request == null) {
            request = new AdminWordLanguageRequest();
        }
        if (request.getStatus() == null) {
            request.setStatus(1);
        }
        return Result.success(languageService.list(request));
    }

    @PostMapping("/category/list")
    public Result<List<AdminWordCategoryResponse>> categoryList(
        @RequestBody(required = false) AdminWordCategoryRequest request
    ) {
        if (request == null) {
            request = new AdminWordCategoryRequest();
        }
        if (request.getStatus() == null) {
            request.setStatus(1);
        }
        return Result.success(categoryService.list(request));
    }

    @PostMapping("/book/list")
    public Result<List<AdminWordBookResponse>> bookList(@RequestBody(required = false) AdminWordBookRequest request) {
        if (request == null) {
            request = new AdminWordBookRequest();
        }
        if (request.getStatus() == null) {
            request.setStatus(1);
        }
        if (request.getPageSize() == null) {
            request.setPageSize(500);
        }
        return Result.success(bookService.list(request));
    }

    @PostMapping("/book/current")
    public Result<UserWordCurrentBookResponse> current(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody(required = false) UserWordCurrentBookRequest request
    ) {
        try {
            UserWordCurrentBookRequest query = request == null ? new UserWordCurrentBookRequest() : request;
            query.setUserId(requireUserId(token));
            List<UserWordCurrentBookResponse> list = currentBookService.list(query);
            UserWordCurrentBookResponse current = list.isEmpty() ? null : list.get(0);
            return Result.success(current == null ? null : learnRecordService.enrichHome(current));
        } catch (IllegalArgumentException e) {
            return Result.error(401, e.getMessage());
        }
    }

    @PostMapping("/book/switch")
    public Result<UserWordCurrentBookResponse> switchBook(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody UserWordCurrentBookRequest request
    ) {
        try {
            BigDecimal userId = requireUserId(token);
            UserWordCurrentBookRequest query = new UserWordCurrentBookRequest();
            query.setUserId(userId);
            List<UserWordCurrentBookResponse> existsList = currentBookService.list(query);
            UserWordCurrentBookResponse exists = existsList.isEmpty() ? null : existsList.get(0);
            request.setUserId(userId);
            if (exists == null) {
                currentBookService.create(request);
            } else {
                request.setId(exists.getId());
                currentBookService.update(request);
            }
            List<UserWordCurrentBookResponse> latest = currentBookService.list(query);
            return Result.success(latest.isEmpty() ? null : latest.get(0));
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("未登录")) {
                return Result.error(401, e.getMessage());
            }
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/plan/get")
    public Result<UserWordStudyPlanResponse> planGet(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody UserWordStudyPlanRequest request
    ) {
        try {
            if (request == null || !StringUtils.hasText(request.getBookCode())) {
                return Result.error("单词本code不能为空");
            }
            UserWordStudyPlanRequest query = new UserWordStudyPlanRequest();
            query.setUserId(requireUserId(token));
            query.setBookCode(request.getBookCode().trim());
            List<UserWordStudyPlanResponse> list = studyPlanService.list(query);
            return Result.success(list.isEmpty() ? null : list.get(0));
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("未登录")) {
                return Result.error(401, e.getMessage());
            }
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/plan/confirm")
    public Result<UserWordStudyPlanResponse> planConfirm(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody UserWordStudyPlanRequest request
    ) {
        try {
            if (request == null) {
                return Result.error("请求不能为空");
            }
            request.setUserId(requireUserId(token));
            return Result.success(studyPlanService.confirm(request));
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("未登录")) {
                return Result.error(401, e.getMessage());
            }
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/study/session")
    public Result<UserWordStudySessionResponse> studySession(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody(required = false) UserWordStudySessionRequest request
    ) {
        try {
            UserWordStudySessionRequest query = request == null ? new UserWordStudySessionRequest() : request;
            query.setUserId(requireUserId(token));
            return Result.success(learnRecordService.createSession(query));
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("未登录")) {
                return Result.error(401, e.getMessage());
            }
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/study/answer")
    public Result<UserWordStudyAnswerResponse> studyAnswer(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody UserWordStudyAnswerRequest request
    ) {
        try {
            if (request == null) {
                return Result.error("请求不能为空");
            }
            request.setUserId(requireUserId(token));
            return Result.success(learnRecordService.answer(request));
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("未登录")) {
                return Result.error(401, e.getMessage());
            }
            return Result.error(e.getMessage());
        }
    }

    private BigDecimal requireUserId(String token) {
        AuthUserInfoResponse user = authService.getCurrentUser(token);
        if (user == null || !StringUtils.hasText(user.getId())) {
            throw new IllegalArgumentException("未登录或登录已过期");
        }
        String account = user.getId().trim();
        try {
            return new BigDecimal(account);
        } catch (NumberFormatException ignored) {
            // 微信一键登录 token 里存的是 openid，落到注册结果表的 user_id
        }
        LambdaQueryWrapper<UserRegistrationResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRegistrationResult::getAccountType, AccountTypeEnum.WECHAT.code())
            .eq(UserRegistrationResult::getAccount, account)
            .orderByDesc(UserRegistrationResult::getId)
            .last("limit 1");
        UserRegistrationResult result = registrationResultMapper.selectOne(wrapper);
        if (result == null || result.getUserId() == null) {
            throw new IllegalArgumentException("未找到用户ID，请重新登录");
        }
        return result.getUserId();
    }
}
