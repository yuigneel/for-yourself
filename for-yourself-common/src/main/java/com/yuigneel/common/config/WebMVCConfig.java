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
                .excludePathPatterns(gateWayProperties.getInsideList()) // 内部调用白名单
                .order(2);
    }
}
