package com.arelore.server.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * 管理员服务启动类。
 * 排除 C 端认证/注册短信实现；启用用户库后可查注册用户与背单词进度。
 */
@SpringBootApplication
@ComponentScan(
    basePackages = "com.arelore.server",
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.arelore\\.server\\.core\\.registration\\.sms\\..*"),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com\\.arelore\\.server\\.core\\.biz\\.user\\.(AuthServiceImpl|UserRegistrationServiceImpl|UserAuthSessionServiceImpl)"
        ),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.arelore\\.server\\.user\\..*")
    }
)
public class AdminServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
    }
}
