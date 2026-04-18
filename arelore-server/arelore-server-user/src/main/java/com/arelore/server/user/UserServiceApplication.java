package com.arelore.server.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 用户服务启动类
 */
@Slf4j
@SpringBootApplication
@ComponentScan("com.arelore.server")
public class UserServiceApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(UserServiceApplication.class, args);
        log.info(
            "User service started successfully. appName={}, webApplicationType={}, activeProfiles={}",
            context.getEnvironment().getProperty("spring.application.name", "arelore-server-user"),
            context.getEnvironment().getProperty("spring.main.web-application-type", WebApplicationType.SERVLET.name()),
            String.join(",", context.getEnvironment().getActiveProfiles())
        );
    }
}
