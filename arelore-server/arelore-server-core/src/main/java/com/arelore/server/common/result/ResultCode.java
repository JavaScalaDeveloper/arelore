package com.arelore.server.common.result;

import lombok.Getter;

/**
 * 通用结果状态码枚举
 */
@Getter
public enum ResultCode {

    // 成功状态码
    SUCCESS(200, "操作成功"),
    
    // 客户端错误
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "拒绝访问"),
    NOT_FOUND(404, "请求资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    
    // 服务端错误
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    
    // 业务错误
    BUSINESS_ERROR(1001, "业务异常"),
    DATA_NOT_FOUND(1002, "数据不存在"),
    DATA_ALREADY_EXISTS(1003, "数据已存在"),
    PARAMS_VALIDATION_ERROR(1004, "参数校验失败"),
    
    // 用户相关
    USER_NOT_LOGIN(2001, "用户未登录"),
    USER_ACCOUNT_DISABLED(2002, "用户账号已被禁用"),
    USER_PASSWORD_ERROR(2003, "用户名或密码错误"),
    USER_TOKEN_EXPIRED(2004, "Token 已过期"),
    USER_TOKEN_INVALID(2005, "Token 无效"),
    USER_REGISTER_APPLY_FAILED(2006, "注册申请失败"),
    USER_MOBILE_ALREADY_REGISTERED(2007, "该手机号已注册"),
    USER_REGISTER_RATE_LIMITED(2008, "操作过于频繁，请稍后再试"),
    USER_REGISTER_PARAM_INVALID(2009, "注册参数不合法"),
    
    // 系统相关
    SYSTEM_BUSY(3001, "系统繁忙，请稍后再试"),
    SYSTEM_MAINTENANCE(3002, "系统维护中"),
    
    // 文件相关
    FILE_UPLOAD_FAILED(4001, "文件上传失败"),
    FILE_DOWNLOAD_FAILED(4002, "文件下载失败"),
    FILE_NOT_FOUND(4003, "文件不存在"),
    FILE_TYPE_NOT_ALLOWED(4004, "不支持的文件类型");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 消息
     */
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
