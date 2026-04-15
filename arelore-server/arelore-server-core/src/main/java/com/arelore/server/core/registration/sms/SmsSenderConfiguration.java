package com.arelore.server.core.registration.sms;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 显式兜底 SmsSender Bean，避免因条件装配导致注入失败。
 */
@Configuration
public class SmsSenderConfiguration {

    @Bean
    @Primary
    public SmsSender smsSender(ObjectProvider<AliyunSmsSender> aliyunProvider, ObjectProvider<NoopSmsSender> noopProvider) {
        try {
            AliyunSmsSender aliyun = aliyunProvider.getIfAvailable();
            if (aliyun != null) {
                return aliyun;
            }
        } catch (Exception ignored) {
            // aliyun bean 初始化失败时自动降级到 noop，保证服务可启动
        }

        NoopSmsSender noop = noopProvider.getIfAvailable();
        if (noop != null) {
            return noop;
        }

        return new NoopSmsSender();
    }
}

