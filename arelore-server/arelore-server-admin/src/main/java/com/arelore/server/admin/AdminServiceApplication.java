package com.arelore.server.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * 管理员服务启动类。
 * 只扫描 education 侧能力；用户库 Mapper/Service 依赖 spring.datasource.user，管理端未配置该数据源。
 */
@SpringBootApplication
@ComponentScan(
    basePackages = "com.arelore.server",
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.arelore\\.server\\.core\\.registration\\.sms\\..*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.arelore\\.server\\.core\\.biz\\.user\\..*ServiceImpl"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.arelore\\.server\\.user\\..*")
    }
)
public class AdminServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
    }
}
