package com.arelore.server.common.constant;

/**
 * 通用常量类
 */
public final class CommonConstants {

    private CommonConstants() {
        throw new IllegalStateException("Constant class cannot be instantiated");
    }

    /**
     * 成功状态码
     */
    public static final Integer SUCCESS_CODE = 200;

    /**
     * 失败状态码
     */
    public static final Integer ERROR_CODE = 500;

    /**
     * 未授权状态码
     */
    public static final Integer UNAUTHORIZED_CODE = 401;

    /**
     * 禁止访问状态码
     */
    public static final Integer FORBIDDEN_CODE = 403;

    /**
     * 未找到状态码
     */
    public static final Integer NOT_FOUND_CODE = 404;

    /**
     * 是
     */
    public static final Integer YES = 1;

    /**
     * 否
     */
    public static final Integer NO = 0;

    /**
     * 默认页码
     */
    public static final Integer DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页大小
     */
    public static final Integer DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大分页大小
     */
    public static final Integer MAX_PAGE_SIZE = 100;

    /**
     * JWT Token 前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * JWT Token Header
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * UTF-8 编码
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK 编码
     */
    public static final String GBK = "GBK";

    /**
     * 应用分隔符
     */
    public static final String SEPARATOR = ":";

    /**
     * 逗号分隔符
     */
    public static final String COMMA = ",";

    /**
     * 分号分隔符
     */
    public static final String SEMICOLON = ";";

    /**
     * 冒号分隔符
     */
    public static final String COLON = ":";

    /**
     * 空格
     */
    public static final String SPACE = " ";

    /**
     * 空字符串
     */
    public static final String EMPTY = "";

    /**
     * HTTP 协议头
     */
    public static final String HTTP_PREFIX = "http://";

    /**
     * HTTPS 协议头
     */
    public static final String HTTPS_PREFIX = "https://";

    /**
     * 文件路径分隔符
     */
    public static final String FILE_SEPARATOR = "/";

    /**
     * Windows 文件路径分隔符
     */
    public static final String WINDOWS_FILE_SEPARATOR = "\\";

    /**
     * Linux 文件路径分隔符
     */
    public static final String LINUX_FILE_SEPARATOR = "/";

    /**
     * 管理员角色标识
     */
    public static final String ROLE_ADMIN = "ADMIN";

    /**
     * 普通用户角色标识
     */
    public static final String ROLE_USER = "USER";

    /**
     * 超级管理员标识
     */
    public static final String SUPER_ADMIN = "super_admin";

    /**
     * 启用状态
     */
    public static final String STATUS_ENABLE = "ENABLE";

    /**
     * 禁用状态
     */
    public static final String STATUS_DISABLE = "DISABLE";

    /**
     * 删除标志：未删除
     */
    public static final Integer DELETED_NO = 0;

    /**
     * 删除标志：已删除
     */
    public static final Integer DELETED_YES = 1;

    /**
     * 邮箱正则表达式
     */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";

    /**
     * 手机号正则表达式
     */
    public static final String MOBILE_REGEX = "^1[3-9]\\d{9}$";

    /**
     * 身份证号正则表达式
     */
    public static final String ID_CARD_REGEX = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$";

    /**
     * URL 正则表达式
     */
    public static final String URL_REGEX = "^(https？|ftp)://[^\\s/$.?#].[^\\s]*$";

    /**
     * IP 地址正则表达式
     */
    public static final String IP_REGEX = "^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$";
}
