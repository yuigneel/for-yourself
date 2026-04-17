package com.yulgnier.gateway.config;
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

        // ====================== 核心配置：允许的源（你的场景只配8081）======================
        // 含义：明确告诉浏览器，只允许 http://localhost:8081 这个地址跨域访问网关
        // 以后换前端地址，只需要改这里！多个地址就多次调用 addAllowedOriginPattern
        corsConfig.addAllowedOriginPattern("http://localhost:8081");

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