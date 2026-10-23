package com.arelore.server.core.common.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * 业务统一使用东八区（Asia/Shanghai），避免服务器 OS 为 UTC 时 LocalDateTime.now() / MySQL CURRENT_TIMESTAMP 差 8 小时。
 */
public final class ChinaTime {
    public static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    public static final String ZONE_ID = "Asia/Shanghai";
    public static final String JDBC_SESSION_TIME_ZONE = "+08:00";
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ChinaTime() {
    }

    /** 启动最早调用：固定 JVM 默认时区 */
    public static void installAsDefault() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZONE));
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }

    /** 东八区当前自然日 */
    public static LocalDate nowDate() {
        return LocalDate.now(ZONE);
    }

    public static String nowDateTime() {
        return now().format(DATETIME);
    }
}
