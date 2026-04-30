package com.yuigneel.center.admin.user.config;

import com.yuigneel.center.admin.user.converter.GenderEnumConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 性别枚举转换器注册器
 * 专属职责：仅注册GenderEnum相关的转换器，不掺杂其他MVC配置
 * 原理：实现WebMvcConfigurer接口，通过addFormatters方法将自定义转换器加入Spring MVC转换器列表
 */
@Configuration
public class GenderEnumConverterConfig implements WebMvcConfigurer {

    /**
     * 注册性别枚举转换器到 Spring容器
     * @param registry Spring MVC的转换器注册器，用于管理所有类型转换器
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 仅注册GenderEnum专属转换器，保证配置的单一性
        registry.addConverter(new GenderEnumConverter());
    }
}