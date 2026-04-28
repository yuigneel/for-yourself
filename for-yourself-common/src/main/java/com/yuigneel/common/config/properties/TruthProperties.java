package com.yuigneel.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * truth属性，用于网关添加header，各个微服务来验证header是否存在来防止恶意请求
 */
@Data
@Component
@ConfigurationProperties(prefix = "truth")
public class TruthProperties {
    private String truthKey;
    private String truthValue;
}
