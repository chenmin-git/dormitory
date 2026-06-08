package com.dormitory.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一 java.time 的 JSON 序列化/反序列化格式：
 * 同时支持请求入参（如 "2026-06-10 08:00:00"）与返回出参的可读格式。
 */
@Configuration
public class JacksonConfig {

    private static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE = "yyyy-MM-dd";
    private static final String TIME = "HH:mm:ss";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2Customizer() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_TIME);
        DateTimeFormatter df = DateTimeFormatter.ofPattern(DATE);
        DateTimeFormatter tf = DateTimeFormatter.ofPattern(TIME);

        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dtf));
            module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dtf));
            module.addSerializer(LocalDate.class, new LocalDateSerializer(df));
            module.addDeserializer(LocalDate.class, new LocalDateDeserializer(df));
            module.addSerializer(LocalTime.class, new LocalTimeSerializer(tf));
            module.addDeserializer(LocalTime.class, new LocalTimeDeserializer(tf));
            // 用 modulesToInstall 追加，保留自动注册的其它模块
            builder.modulesToInstall(module);
        };
    }
}
