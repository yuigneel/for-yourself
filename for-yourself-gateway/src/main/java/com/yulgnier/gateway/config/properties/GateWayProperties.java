package com.yulgnier.gateway.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "gateway")
public class GateWayProperties {
    private String[] whiteList; // 白名单
}
