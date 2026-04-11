package com.yulgnier.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component // 交给 Spring管理
@ConfigurationProperties(prefix = "snowflake") // 绑定 yml前缀
public class SnowflakeProperties {

    // 数据中心ID，默认0，分布式改这个
    private Integer dataCenterId = 0;

    // 机器ID，默认0，分布式改这个
    private Integer workerId = 0;
}
