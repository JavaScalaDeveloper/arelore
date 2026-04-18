package com.arelore.server.core.registration.enums;

/**
 * 注册账号类型枚举。
 * 目前仅支持手机号注册，后续可扩展 WECHAT/EMAIL 等类型。
 */
public enum AccountTypeEnum {
    MOBILE("MOBILE"),
    /**
     * 用于 user_registration_application 中记录找回密码验证码申请流水。
     */
    MOBILE_PASSWORD_RESET("MOBILE_PASSWORD_RESET");

    private final String code;

    AccountTypeEnum(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}

