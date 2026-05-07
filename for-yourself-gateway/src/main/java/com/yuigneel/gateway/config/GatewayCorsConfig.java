/*
 * For Yourself - A graduation project by the author, serving as a demonstration for the future complete project ecosystem
 * Copyright (C) 2026  Yu·Igneel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.yuigneel.gateway.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 网关全局跨域配置
 * 场景：前端是 8081 端口(Knife4j)，后端网关是 8080 端口，解决跨域拦截
 * 核心作用：网关自己处理 OPTIONS 预检请求，不再转发给微服务
 */
@Configuration
public class GatewayCorsConfig {

    /**
     * 注册跨域过滤器（网关是WebFlux响应式，必须用这个Bean，不能用MVC的跨域配置）
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        // 1. 创建跨域配置对象
        CorsConfiguration corsConfig = new CorsConfiguration();

        // ====================== 核心配置：允许的源（你的场景配8081和8082）======================
        // 含义：明确告诉浏览器，允许 http://localhost:8081 和 http://localhost:8082 这两个地址跨域访问网关
        // 以后换前端地址，只需要改这里！多个地址就多次调用 addAllowedOriginPattern
        corsConfig.addAllowedOriginPattern("http://localhost:8081");
        corsConfig.addAllowedOriginPattern("http://localhost:8082");

        // ====================== 允许的请求头 ======================
        // 含义：允许所有类型的请求头（比如Token、Content-Type等）
        // 前端传自定义请求头（如Authorization）必须开这个
        corsConfig.addAllowedHeader("*");

        // ====================== 允许的请求方法 ======================
        // 含义：允许所有请求方法（GET/POST/PUT/DELETE/OPTIONS）
        // 关键：包含了OPTIONS预检请求，网关会自动处理
        corsConfig.addAllowedMethod("*");

        // ====================== 允许携带身份凭证 ======================
        // 含义：允许跨域传递Cookie、Token等身份信息
        // 登录接口必须开启，否则前端无法传递登录凭证
        corsConfig.setAllowCredentials(true);

        // ====================== 预检请求缓存时间 ======================
        // 单位：秒，这里设置1小时
        // 作用：同一个跨域请求，1小时内不再重复发OPTIONS预检，提升速度
        corsConfig.setMaxAge(3600L);

        // 2. 注册跨域配置：对所有接口生效(**代表所有路径)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        // 3. 返回跨域过滤器
        return new CorsWebFilter(source);
    }
}