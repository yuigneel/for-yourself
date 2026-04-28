package com.yuigneel.gateway.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
/**
 * 动态路由配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway.routes")
public class DynamicRouteLoaderProperties {
    private String group;
    private String dataId;
}
