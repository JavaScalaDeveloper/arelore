package com.arelore.server.user.controller;

import com.arelore.server.core.biz.prompt.user.UserAiPromptService;
import com.arelore.server.core.biz.prompt.user.UserAiPromptTagService;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptImportResult;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptRequest;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptResponse;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptTagRequest;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptTagResponse;
import com.arelore.server.core.biz.user.AuthService;
import com.arelore.server.core.biz.user.dto.AuthUserInfoResponse;
import com.arelore.server.core.common.dto.PageResult;
import com.arelore.server.core.common.result.Result;
import com.arelore.server.core.registration.entity.UserRegistrationResult;
import com.arelore.server.core.registration.enums.AccountTypeEnum;
import com.arelore.server.core.registration.mapper.UserRegistrationResultMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/user/prompt")
public class AiPromptController {

    private final UserAiPromptService promptService;
    private final UserAiPromptTagService tagService;
    private final AuthService authService;
    private final UserRegistrationResultMapper registrationResultMapper;

    public AiPromptController(
        UserAiPromptService promptService,
        UserAiPromptTagService tagService,
        AuthService authService,
        UserRegistrationResultMapper registrationResultMapper
    ) {
        this.promptService = promptService;
        this.tagService = tagService;
        this.authService = authService;
        this.registrationResultMapper = registrationResultMapper;
    }

    @PostMapping("/page")
    public Result<PageResult<UserAiPromptResponse>> page(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody UserAiPromptRequest request
    ) {
        return run(() -> {
            UserAiPromptRequest body = request == null ? new UserAiPromptRequest() : request;
            body.setUserId(requireUserId(token));
            return Result.success(promptService.pageQuery(body));
        });
    }

    @PostMapping("/list")
    public Result<List<UserAiPromptResponse>> list(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody(required = false) UserAiPromptRequest request
    ) {
        return run(() -> {
            UserAiPromptRequest body = request == null ? new UserAiPromptRequest() : request;
            body.setUserId(requireUserId(token));
            return Result.success(promptService.list(body));
        });
    }

    @PostMapping("/detail")
    public Result<UserAiPromptResponse> detail(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody UserAiPromptRequest request
    ) {
        return run(() -> {
            BigDecimal userId = requireUserId(token);
            if (request == null || request.getId() == null) {
                return Result.error("id不能为空");
            }
            UserAiPromptResponse detail = promptService.getById(request.getId());
            if (detail == null || detail.getUserId() == null || detail.getUserId().compareTo(userId) != 0) {
                return Result.error("记录不存在");
            }
            return Result.success(detail);
        });
    }

    @PostMapping("/save")
    public Result<UserAiPromptResponse> save(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody UserAiPromptRequest request
    ) {
        return run(() -> {
            if (request == null) {
                return Result.error("请求不能为空");
            }
            request.setUserId(requireUserId(token));
            return Result.success(promptService.saveOrUpsert(request));
        });
    }

    @PostMapping("/delete")
    public Result<Void> delete(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody UserAiPromptRequest request
    ) {
        return run(() -> {
            if (request == null || request.getId() == null) {
                return Result.error("id不能为空");
            }
            promptService.softDelete(requireUserId(token), request.getId());
            return Result.success();
        });
    }

    @PostMapping("/import")
    public Result<UserAiPromptImportResult> importBatch(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody UserAiPromptRequest request
    ) {
        return run(() -> Result.success(promptService.importBatch(requireUserId(token), request)));
    }

    @PostMapping("/export")
    public Result<List<UserAiPromptResponse>> export(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody(required = false) UserAiPromptRequest request
    ) {
        return run(() -> {
            UserAiPromptRequest body = request == null ? new UserAiPromptRequest() : request;
            body.setUserId(requireUserId(token));
            return Result.success(promptService.list(body));
        });
    }

    @PostMapping("/tag/list")
    public Result<List<UserAiPromptTagResponse>> tagList(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody(required = false) UserAiPromptTagRequest request
    ) {
        return run(() -> {
            UserAiPromptTagRequest body = request == null ? new UserAiPromptTagRequest() : request;
            body.setUserId(requireUserId(token));
            return Result.success(tagService.list(body));
        });
    }

    @PostMapping("/tag/save")
    public Result<UserAiPromptTagResponse> tagSave(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody UserAiPromptTagRequest request
    ) {
        return run(() -> {
            if (request == null) {
                return Result.error("请求不能为空");
            }
            request.setUserId(requireUserId(token));
            return Result.success(tagService.saveTag(request));
        });
    }

    @PostMapping("/tag/delete")
    public Result<Void> tagDelete(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody UserAiPromptTagRequest request
    ) {
        return run(() -> {
            if (request == null || request.getId() == null) {
                return Result.error("id不能为空");
            }
            tagService.softDelete(requireUserId(token), request.getId());
            return Result.success();
        });
    }

    private <T> Result<T> run(ResultSupplier<T> supplier) {
        try {
            return supplier.get();
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
            // 微信 openid
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

    @FunctionalInterface
    private interface ResultSupplier<T> {
        Result<T> get();
    }
}
