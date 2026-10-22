package com.arelore.server.core.common.config;

import com.arelore.server.core.common.time.ChinaTime;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * 确保 Spring 容器内默认时区为东八区（与 jackson / JDBC session 一致）。
 */
@Configuration
public class ApplicationTimezoneConfig {
    private static final Logger log = LoggerFactory.getLogger(ApplicationTimezoneConfig.class);

    @PostConstruct
    public void applyChinaTimeZone() {
        ChinaTime.installAsDefault();
        log.info("Default timezone set to {} (jvm={}, zoneId={})",
            ChinaTime.ZONE_ID,
            TimeZone.getDefault().getID(),
            ZoneId.systemDefault());
    }
}
