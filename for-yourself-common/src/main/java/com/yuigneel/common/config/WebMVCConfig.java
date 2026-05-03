package com.yuigneel.common.config;

import com.yuigneel.common.config.properties.GateWayProperties;
import com.yuigneel.common.config.properties.TruthProperties;
import com.yuigneel.common.interceptors.UserInfoInterceptor;
import com.yuigneel.common.interceptors.UserStatusInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 配置拦截器，用于处理用户信息
 *
 * @author yulgnier
 */

@RequiredArgsConstructor
@Configuration
public class WebMVCConfig implements WebMvcConfigurer {
    private final GateWayProperties gateWayProperties;
    private final TruthProperties truthProperties;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        // 拦截器1：认证拦截器 (Order 1)
        // 职责：验证 truth header、提取 uid/link 并存入 ThreadLocal
        registry.addInterceptor(new UserInfoInterceptor(truthProperties))
                .addPathPatterns("/**")
                .excludePathPatterns(gateWayProperties.getWhiteList())
                .order(1);

        // 拦截器2：状态校验拦截器 (Order 2)
        // 职责：发布事件校验账号是否被封禁
        registry.addInterceptor(new UserStatusInterceptor(eventPublisher))
                .addPathPatterns("/**")
                .excludePathPatterns(gateWayProperties.getWhiteList())
                .order(2);
    }
}
