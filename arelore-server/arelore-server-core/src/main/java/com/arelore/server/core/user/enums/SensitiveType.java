package com.arelore.server.core.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 敏感信息类型枚举
 */
@Getter
@AllArgsConstructor
public enum SensitiveType {
    CHINA_PHONE("china_phone", "中国手机号", "13812345678"),
    INTL_PHONE("intl_phone", "国际手机号", "+86-138-1234-5678"),
    ADDRESS("address", "地址", "北京市朝阳区建国路 88 号"),
    CHINESE_NAME("chinese_name", "汉语姓名", "张三"),
    ENGLISH_NAME("english_name", "英文姓名", "John Smith"),
    EMAIL("email", "邮箱", "zhangsan@email.com"),
    ID_CARD("id_card", "身份证号", "110101199001011234"),
    BANK_CARD("bank_card", "银行卡号", "6222 0218 0920 0000"),
    PASSPORT("passport", "护照号码", "E12345678"),
    IP_ADDRESS("ip_address", "IP 地址", "192.168.1.1"),
    URL("url", "URL 链接", "https://www.example.com/path"),
    LICENSE_PLATE("license_plate", "车牌号", "京 A12345"),
    CUSTOM_REGEX("custom_regex", "自定义正则表达式", "正则表达式");

    private final String code;
    private final String name;
    private final String example;

    public static SensitiveType fromCode(String code) {
        for (SensitiveType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的敏感类型：" + code);
    }
}
