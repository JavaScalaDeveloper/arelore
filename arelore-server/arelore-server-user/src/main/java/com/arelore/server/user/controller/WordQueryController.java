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
import com.arelore.server.core.biz.word.user.UserWordCurrentBookService;
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
    private final AuthService authService;
    private final UserRegistrationResultMapper registrationResultMapper;

    public WordQueryController(
        AdminWordLanguageService languageService,
        AdminWordCategoryService categoryService,
        AdminWordBookService bookService,
        UserWordCurrentBookService currentBookService,
        AuthService authService,
        UserRegistrationResultMapper registrationResultMapper
    ) {
        this.languageService = languageService;
        this.categoryService = categoryService;
        this.bookService = bookService;
        this.currentBookService = currentBookService;
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
            return Result.success(list.isEmpty() ? null : list.get(0));
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
