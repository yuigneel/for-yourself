package com.yulgnier.center.admin.user.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 杂项类配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "yulgnier")
public class MiscellaneousProperties {
    private Integer emailExpireMinutes;
    private Integer emailTryTimes;
}
