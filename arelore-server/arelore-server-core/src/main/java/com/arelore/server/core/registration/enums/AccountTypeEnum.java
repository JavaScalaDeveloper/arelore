package com.arelore.server.core.registration.enums;

/**
 * 注册账号类型枚举。
 * 目前仅支持手机号注册，后续可扩展 WECHAT/EMAIL 等类型。
 */
public enum AccountTypeEnum {
    MOBILE("MOBILE"),
    WECHAT("WECHAT"),
    /**
     * 用于 user_registration_application 中记录找回密码验证码申请流水。
     */
    MOBILE_PASSWORD_RESET("MOBILE_PASSWORD_RESET"),
    /**
     * 小程序“手册登录”（手机号+密码）登录流水。
     */
    MOBILE_MINI_MANUAL_LOGIN("MOBILE_MINI_MANUAL_LOGIN");

    private final String code;

    AccountTypeEnum(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}

