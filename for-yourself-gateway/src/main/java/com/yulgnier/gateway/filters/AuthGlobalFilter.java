package com.yulgnier.gateway.filters;

import com.yulgnier.common.exception.ForYourselfException;
import com.yulgnier.common.model.result.ResultCodeEnum;
import com.yulgnier.common.utils.JwtUtil;
import com.yulgnier.gateway.config.properties.GateWayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    private final GateWayProperties gateWayProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * 鉴权拦截器
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取请求头
        ServerHttpRequest request = exchange.getRequest();
        // 判断是否需要拦截
        if (isExcludePath(request.getPath().toString())) {
            return chain.filter(exchange);
        }
        // 校验并解析token
        //   获取jwt(“Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9”)
        String token = null;
        token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        // 严格校验Bearer格式（杜绝数组越界）
        if (token == null || !token.startsWith("Bearer ")) {
            return Mono.error(new ForYourselfException(ResultCodeEnum.ADMIN_LOGIN_REQUIRED, null));
        } else token = token.split(" ")[1];

        Map<String, Object> claimsFromToken = JwtUtil.getClaimsFromToken(token); // 解析token,token无效返回null
        if (claimsFromToken == null) {
            return Mono.error(new ForYourselfException(ResultCodeEnum.ADMIN_LOGIN_REQUIRED, null));
        }
        //TODO =================================================
        // 放行
        return chain.filter(exchange);
    }

    /**
     * 定义一堆过滤器的优先级数值越大的优先级越低
     *
     * @return 优先级
     */
    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 判断是否需要拦截
     *
     * @param path 请求路径
     * @return true:需要拦截
     */
    private boolean isExcludePath(String path) {
        for (String excludePath : gateWayProperties.getWhiteList()) {
            if (antPathMatcher.match(excludePath, path)) {
                return true;
            }
        }
        return false;
    }
}
