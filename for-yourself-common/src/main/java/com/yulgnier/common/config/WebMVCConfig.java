package com.yulgnier.common.config;

import com.yulgnier.common.config.properties.GateWayProperties;
import com.yulgnier.common.config.properties.TruthProperties;
import com.yulgnier.common.interceptors.UserInfoInterceptor;
import lombok.RequiredArgsConstructor;
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

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(new UserInfoInterceptor(truthProperties))
                .addPathPatterns("/**")
                .excludePathPatterns(gateWayProperties.getWhiteList());  //  直接传入配置好的白名单数组 ✅
    }
}
