package com.arelore.server.user.controller;

import com.arelore.server.core.common.result.Result;
import com.arelore.server.core.biz.user.dto.ClientLogReportRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前端日志上报控制器。
 */
@Slf4j
@RestController
@RequestMapping("/api/user/client-log")
public class ClientLogController {

    @PostMapping("/report")
    public Result<Void> report(@RequestBody ClientLogReportRequest request) {
        if (request == null || !StringUtils.hasText(request.getMessage())) {
            return Result.success(null);
        }
        String level = StringUtils.hasText(request.getLevel()) ? request.getLevel().trim().toUpperCase() : "INFO";
        String msg = request.getMessage();
        String detail = StringUtils.hasText(request.getDetail()) ? request.getDetail() : "";
        String pageUrl = StringUtils.hasText(request.getPageUrl()) ? request.getPageUrl() : "";
        String userAgent = StringUtils.hasText(request.getUserAgent()) ? request.getUserAgent() : "";

        if ("ERROR".equals(level)) {
            log.error("CLIENT_LOG message={}, detail={}, pageUrl={}, userAgent={}", msg, detail, pageUrl, userAgent);
        } else if ("WARN".equals(level)) {
            log.warn("CLIENT_LOG message={}, detail={}, pageUrl={}, userAgent={}", msg, detail, pageUrl, userAgent);
        } else {
            log.info("CLIENT_LOG message={}, detail={}, pageUrl={}, userAgent={}", msg, detail, pageUrl, userAgent);
        }
        return Result.success(null);
    }
}
