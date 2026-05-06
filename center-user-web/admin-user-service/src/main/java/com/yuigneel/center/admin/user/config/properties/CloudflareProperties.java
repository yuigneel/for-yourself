package com.yuigneel.center.admin.user.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cloudflare Turnstile 配置类
 *
 * @author 羽·伊格尼尔
 * @since 2026-05-06
 */
@Data
@Component
@ConfigurationProperties(prefix = "cloudflare.turnstile") // 绑定 yml前缀
public class CloudflareProperties {
    /**
     * 私密密钥
     */
    private String secret;
}
