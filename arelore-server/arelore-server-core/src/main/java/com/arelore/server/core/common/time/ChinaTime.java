package com.arelore.server.core.common.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.TimeZone;

/**
 * 业务统一使用东八区（Asia/Shanghai），避免服务器 OS 为 UTC 时 LocalDateTime.now() / MySQL CURRENT_TIMESTAMP 差 8 小时。
 * <p>
 * 写入 JSON / ext_info 的时间字符串请一律用 {@link #nowDateTime()} / {@link #format(LocalDateTime)}，
 * 格式固定为 {@value #DATETIME_PATTERN}（不要用 {@code LocalDateTime#toString()}）。
 */
public final class ChinaTime {
    public static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    public static final String ZONE_ID = "Asia/Shanghai";
    public static final String JDBC_SESSION_TIME_ZONE = "+08:00";
    /** 全局统一日期时间格式 */
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

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

    /** 当前时间字符串：yyyy-MM-dd HH:mm:ss */
    public static String nowDateTime() {
        return format(now());
    }

    /** 格式化为 yyyy-MM-dd HH:mm:ss；null 返回 null */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATETIME);
    }

    /**
     * 解析时间字符串：优先 yyyy-MM-dd HH:mm:ss，兼容历史 ISO（含 T / 纳秒）。
     */
    public static LocalDateTime parseDateTime(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String s = text.trim();
        try {
            return LocalDateTime.parse(s, DATETIME);
        } catch (DateTimeParseException ignored) {
            // fall through
        }
        try {
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException e) {
            throw new DateTimeParseException(
                "无法解析时间，期望 " + DATETIME_PATTERN + " 或 ISO-8601: " + s,
                s,
                0,
                e
            );
        }
    }
}
