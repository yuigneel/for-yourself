package com.yulgnier.center.user.api.client;

import com.yulgnier.common.config.DefaultFeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;


/**
 * 普通用户服务 Feign 客户端
 *
 * @author yulgnier
 * @since 2026-04-21
 */
@FeignClient(name = "center-common-user-service", path = "/center-common/user", configuration = DefaultFeignClientConfig.class)
public interface CommonUserClient {

}
