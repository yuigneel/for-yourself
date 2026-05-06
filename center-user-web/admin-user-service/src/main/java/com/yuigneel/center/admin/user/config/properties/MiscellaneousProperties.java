package com.yuigneel.center.admin.user.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 杂项配置类
 *
 * @author 逆羽风辰
 * @since 2026-05-06
 */
@Data
@Component
@ConfigurationProperties(prefix = "yuigneel")
public class MiscellaneousProperties {
    private Integer emailExpireMinutes;
    private Integer emailTryTimes;
}
