package com.arelore.server.common.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@Configuration
public class JacksonDateTimeConfig {
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
            javaTimeModule.addSerializer(java.time.LocalDateTime.class, new LocalDateTimeSerializer(formatter));
            javaTimeModule.addDeserializer(java.time.LocalDateTime.class, new LocalDateTimeDeserializer(formatter));

            builder.modules(javaTimeModule);
            builder.simpleDateFormat(DATETIME_PATTERN);
            builder.timeZone(TimeZone.getTimeZone(ZoneId.of("Asia/Shanghai")));
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.dateFormat(new SimpleDateFormat(DATETIME_PATTERN));
        };
    }
}
