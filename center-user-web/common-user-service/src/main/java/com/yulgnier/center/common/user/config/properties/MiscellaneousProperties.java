package com.yulgnier.center.common.user.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "yulgnier")
public class MiscellaneousProperties {
    private Integer emailExpireMinutes;
}
