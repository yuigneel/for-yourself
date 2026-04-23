package com.yulgnier.gateway.filters;

import com.yulgnier.common.config.properties.TruthProperties;
import com.yulgnier.common.exception.ForYourselfException;
import com.yulgnier.common.model.constants.AuthConstants;
import com.yulgnier.common.model.result.ResultCodeEnum;
import com.yulgnier.common.utils.JwtUtil;
import com.yulgnier.common.config.properties.GateWayProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    private final GateWayProperties gateWayProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final TruthProperties truthProperties;
    /**
     * 鉴权拦截器
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取请求头
        ServerHttpRequest request = exchange.getRequest();
        // 判断是否需要拦截
        if (isExcludePath(request.getPath().toString())) {
            log.debug("请求被放行 - URL: {}", request.getPath());
            return chain.filter(exchange);
        }

        // 校验并解析token
        //   获取jwt("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9")
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        //  严格校验 Bearer 格式
        if (authorization == null || authorization.length() <= 7 || !authorization.startsWith("Bearer ")) {
            return Mono.error(new ForYourselfException(ResultCodeEnum.CAPTCHA_VERIFICATION_FAILED, null));
        }

        //  安全提取JWT token（防止数组越界）
        String token = authorization.substring(7); // "Bearer " 长度为7

        Map<String, Object> claimsFromToken = JwtUtil.getClaimsFromToken(token); // 解析token,token无效返回null
        if (claimsFromToken == null) {
            return Mono.error(new ForYourselfException(ResultCodeEnum.CAPTCHA_VERIFICATION_FAILED, null));
        }

        // 从 token 中获取用户 UID
        Object uidObj = claimsFromToken.get(AuthConstants.UID_KEY);
        if (uidObj == null) {
            return Mono.error(new ForYourselfException(ResultCodeEnum.CAPTCHA_VERIFICATION_FAILED, "Token 中缺少用户信息"));
        }

        String uid = String.valueOf(uidObj);

        // 构建修改后的请求
        ServerHttpRequest mutatedRequest = request.mutate()
                .headers(h -> h.remove(HttpHeaders.AUTHORIZATION))       // ① 删除 JWT Token 头
                .header(AuthConstants.UID_KEY, uid)                      // ② 添加用户 UID
                .build();

        // 添加特殊请求头
        mutatedRequest = mutatedRequest.mutate()
                .header(truthProperties.getTruthKey(), truthProperties.getTruthValue())
                .build();

        // 将修改后的请求传递到过滤器链
        // exchange.mutate() - 创建 ServerWebExchange 的构建器（不可变对象的修改模式）
        // .request(mutatedRequest) - 替换原始请求为修改后的请求对象
        // .build() - 构建新的 ServerWebExchange 实例
        // chain.filter() - 将新的 exchange 传递给下一个过滤器继续处理
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
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
        log.debug("请求被拦截，URL: {}", path);
        return false;
    }
}
