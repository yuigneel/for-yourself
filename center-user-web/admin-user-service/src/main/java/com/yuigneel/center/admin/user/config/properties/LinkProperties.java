package com.yuigneel.center.admin.user.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 内部服务链接配置类
 *
 * @author Yu·Igneel
 * @since 2026-05-06
 */
@Data
@Component
@ConfigurationProperties(prefix = "link")
public class LinkProperties {
    private adminCommon adminCommon;
    @Data
    public static class adminCommon {
        private String secretKey;
        private Long expireMilliseconds;
    }
}
