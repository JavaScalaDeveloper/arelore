package com.arelore.server.admin.dto;

import lombok.Data;

/**
 * 系统设置请求 DTO
 */
@Data
public class SystemSettingsRequest {

    /**
     * 站点名称
     */
    private String siteName;

    /**
     * 站点描述
     */
    private String siteDescription;

    /**
     * 是否允许注册
     */
    private Boolean allowRegister;

    /**
     * 维护模式
     */
    private Boolean maintenanceMode;

    /**
     * SMTP 服务器
     */
    private String smtpHost;

    /**
     * SMTP 端口
     */
    private Integer smtpPort;

    /**
     * SMTP 用户名
     */
    private String smtpUsername;

    /**
     * SMTP 密码
     */
    private String smtpPassword;
}
