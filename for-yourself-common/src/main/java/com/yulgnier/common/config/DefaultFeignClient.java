package com.yulgnier.common.config;

import com.yulgnier.common.model.constants.AuthConstants;
import com.yulgnier.common.utils.UserContextUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;


/**
 * Feign 全局请求配置类（用于统一添加请求头、处理请求拦截）
 * <p>
 * 核心说明：
 * 1. Feign 拥有独立于 Spring 主容器的子上下文，二者相互隔离
 * 2. 此类**禁止添加 @Configuration 注解**，否则会被 Spring 主容器加载，
 * 导致配置全局生效、优先级混乱，且无法正常被 Feign 客户端识别
 * <p>
 * 配置生效两种方式：
 * ① 启动类全局配置：在 @EnableFeignClients 中指定 defaultConfiguration = 当前类.class
 * 作用：对当前模块所有 Feign 客户端统一生效
 * ② 客户端局部配置：在 @FeignClient 接口上指定 configuration = 当前类.class
 * 作用：仅对当前 Feign 客户端生效，无需每个调用模块重复配置
 * <p>
 * 接口上配置生效原因：
 * 显式指定后，Feign 会直接将该配置加载到自身独立上下文，
 * 所有引入此 API 接口的模块，创建对应 Feign 客户端时都会自动应用该配置，
 * 无需在每个模块启动类重复声明，实现一处配置 、多处复用
 */
public class DefaultFeignClient {

    @Value("${truth.truth-key}")
    private String truthKey;

    @Value("${truth.truth-value}")
    private String truthValue;

    /**
     * Feign 请求拦截器：自动添加 truth 和 uid 请求头
     *
     * @return RequestInterceptor
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 添加 truth 请求头（防止恶意请求）
                template.header(truthKey, truthValue);

                // 添加 uid 请求头（从 ThreadLocal 中获取当前用户 UID）
                Long uid = UserContextUtil.getUid();
                if (uid != null) {
                    template.header(AuthConstants.UID_KEY, String.valueOf(uid));
                }
            }
        };
    }
}
