package com.arelore.server.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 用户服务跨域配置
 */
@Configuration
public class UserCorsConfig {

    /**
     * 当前跨域设置
     */
    @Bean
    public CorsFilter userCorsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // Bearer Token 走 Header，不依赖 Cookie；避免 credentials+* 在浏览器被拒
        config.setAllowCredentials(false);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
