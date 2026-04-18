package com.arelore.server.user.dto;

import lombok.Data;

/**
 * 前端日志上报请求体。
 */
@Data
public class ClientLogReportRequest {
    private String level;
    private String message;
    private String detail;
    private String pageUrl;
    private String userAgent;
}
