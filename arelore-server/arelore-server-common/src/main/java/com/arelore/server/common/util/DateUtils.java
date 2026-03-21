package com.arelore.server.common.util;

import cn.hutool.core.date.DateUtil;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 日期时间工具类
 */
public class DateUtils {

    private DateUtils() {
        throw new IllegalStateException("Utility class cannot be instantiated");
    }

    /**
     * 默认日期格式
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 默认时间格式
     */
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    /**
     * 默认日期时间格式
     */
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 获取当前日期字符串
     *
     * @return yyyy-MM-dd
     */
    public static String getCurrentDate() {
        return LocalDate.now().toString();
    }

    /**
     * 获取当前时间字符串
     *
     * @return HH:mm:ss
     */
    public static String getCurrentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT));
    }

    /**
     * 获取当前日期时间字符串
     *
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT));
    }

    /**
     * 格式化日期
     *
     * @param date    日期
     * @param pattern 格式
     * @return 格式化后的字符串
     */
    public static String formatDate(LocalDate date, String pattern) {
        if (date == null) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化时间
     *
     * @param time    时间
     * @param pattern 格式
     * @return 格式化后的字符串
     */
    public static String formatTime(LocalTime time, String pattern) {
        if (time == null) {
            return null;
        }
        return time.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化日期时间
     *
     * @param dateTime 日期时间
     * @param pattern  格式
     * @return 格式化后的字符串
     */
    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 解析日期字符串
     *
     * @param dateStr 日期字符串
     * @param pattern 格式
     * @return LocalDate
     */
    public static LocalDate parseDate(String dateStr, String pattern) {
        if (CommonUtils.isEmpty(dateStr)) {
            return null;
        }
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 解析时间字符串
     *
     * @param timeStr 时间字符串
     * @param pattern 格式
     * @return LocalTime
     */
    public static LocalTime parseTime(String timeStr, String pattern) {
        if (CommonUtils.isEmpty(timeStr)) {
            return null;
        }
        return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 解析日期时间字符串
     *
     * @param dateTimeStr 日期时间字符串
     * @param pattern     格式
     * @return LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeStr, String pattern) {
        if (CommonUtils.isEmpty(dateTimeStr)) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 获取当前时间戳（秒）
     *
     * @return 秒级时间戳
     */
    public static Long getCurrentTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 获取当前时间戳（毫秒）
     *
     * @return 毫秒级时间戳
     */
    public static Long getCurrentTimestampMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 时间戳转日期时间
     *
     * @param timestamp 秒级时间戳
     * @return LocalDateTime
     */
    public static LocalDateTime timestampToDateTime(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
    }

    /**
     * 时间戳转日期时间（毫秒）
     *
     * @param timestampMillis 毫秒级时间戳
     * @return LocalDateTime
     */
    public static LocalDateTime timestampMillisToDateTime(Long timestampMillis) {
        if (timestampMillis == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampMillis), ZoneId.systemDefault());
    }

    /**
     * 日期时间转时间戳
     *
     * @param dateTime 日期时间
     * @return 秒级时间戳
     */
    public static Long dateTimeToTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    /**
     * 日期时间转时间戳（毫秒）
     *
     * @param dateTime 日期时间
     * @return 毫秒级时间戳
     */
    public static Long dateTimeToTimestampMillis(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 计算两个日期之间的天数差
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 天数差
     */
    public static Long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * 计算两个日期时间之间的小时差
     *
     * @param startDateTime 开始日期时间
     * @param endDateTime   结束日期时间
     * @return 小时差
     */
    public static Long hoursBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return null;
        }
        return ChronoUnit.HOURS.between(startDateTime, endDateTime);
    }

    /**
     * 判断是否是今天
     *
     * @param dateTime 日期时间
     * @return true-是今天，false-不是今天
     */
    public static boolean isToday(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.toLocalDate().equals(LocalDate.now());
    }

    /**
     * 判断是否是昨天
     *
     * @param dateTime 日期时间
     * @return true-是昨天，false-不是昨天
     */
    public static boolean isYesterday(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.toLocalDate().equals(LocalDate.now().minusDays(1));
    }

    /**
     * 获取某月的第一天
     *
     * @param date 日期
     * @return 当月第一天
     */
    public static LocalDate getFirstDayOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfMonth(1);
    }

    /**
     * 获取某月的最后一天
     *
     * @param date 日期
     * @return 当月最后一天
     */
    public static LocalDate getLastDayOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    /**
     * 获取某年的第一天
     *
     * @param date 日期
     * @return 当年第一天
     */
    public static LocalDate getFirstDayOfYear(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfYear(1);
    }

    /**
     * 获取某年的最后一天
     *
     * @param date 日期
     * @return 当年最后一天
     */
    public static LocalDate getLastDayOfYear(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfYear(date.lengthOfYear());
    }

    /**
     * Date 转 LocalDateTime
     *
     * @param date Date
     * @return LocalDateTime
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * LocalDateTime 转 Date
     *
     * @param localDateTime LocalDateTime
     * @return Date
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
