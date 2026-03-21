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
        
        // 允许所有域名进行跨域调用（生产环境应该指定具体域名）
        config.addAllowedOriginPattern("*");
        
        // 允许跨域的请求头
        config.addAllowedHeader("*");
        
        // 允许跨域的请求方法
        config.addAllowedMethod("*");
        
        // 允许携带认证信息（cookies、HTTP 认证等）
        config.setAllowCredentials(true);
        
        // 预检请求的缓存时间（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径生效
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
