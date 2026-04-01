package com.yulgnier.center.common.user.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cloudflare 配置类（绑定yml配置）
 */
@Data
@Component
@ConfigurationProperties(prefix = "cloudflare.turnstile") // 绑定yml前缀
public class CloudflareProperties {
    /**
     * 私密密钥
     */
    private String secret;
}
