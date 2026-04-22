package com.yulgnier.center.admin.user.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
