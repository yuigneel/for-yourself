package com.yulgnier.common.config;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.yulgnier.common.config.properties.SnowflakeProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // 配置类标记
public class SnowflakeConfig {

    // 注入配置
    private final SnowflakeProperties properties;

    // 构造器注入（比@Autowired更规范）
    public SnowflakeConfig(SnowflakeProperties properties) {
        this.properties = properties;
    }

    // 创建雪花算法Bean，全局单例
    @Bean
    public Snowflake snowflake() {
        return IdUtil.createSnowflake(
                properties.getWorkerId(),
                properties.getDataCenterId()
        );
    }
}
