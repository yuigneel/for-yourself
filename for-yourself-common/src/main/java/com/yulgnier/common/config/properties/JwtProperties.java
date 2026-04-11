package com.yulgnier.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt") // 绑定 yml 前缀
public class JwtProperties {
    private String secretKey;
    private String issuer;
    private Integer expireHour;
}
