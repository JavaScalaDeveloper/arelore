package com.arelore.server.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UserWebMvcConfig implements WebMvcConfigurer {
    private final UserDailyVisitInterceptor dailyVisitInterceptor;

    public UserWebMvcConfig(UserDailyVisitInterceptor dailyVisitInterceptor) {
        this.dailyVisitInterceptor = dailyVisitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(dailyVisitInterceptor)
            .addPathPatterns("/api/user/**")
            // 登录/发码等未登录接口不统计
            .excludePathPatterns(
                "/api/user/auth/**",
                "/api/user/mini/auth/**",
                "/api/user/register/**",
                "/api/user/common-binary-file/**",
                "/api/user/client-log/**"
            );
    }
}
