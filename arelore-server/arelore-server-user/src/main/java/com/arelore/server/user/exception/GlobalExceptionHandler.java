package com.arelore.server.user.exception;

import com.arelore.server.common.exception.BusinessException;
import com.arelore.server.common.result.Result;
import com.arelore.server.common.result.ResultCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), sanitizeMessage(e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return Result.error(ResultCode.USER_REGISTER_PARAM_INVALID, sanitizeMessage(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        return Result.error(ResultCode.INTERNAL_SERVER_ERROR, "系统繁忙，请稍后再试");
    }

    private String sanitizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "操作失败，请稍后再试";
        }
        String lower = message.toLowerCase();
        if (lower.contains("sql")
            || lower.contains("jdbc")
            || lower.contains("table")
            || lower.contains("column")
            || lower.contains("constraint")
            || lower.contains("exception")) {
            return "操作失败，请稍后再试";
        }
        return message;
    }
}

