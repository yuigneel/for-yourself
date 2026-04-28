package com.yuigneel.center.admin.user.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 邮箱配置类（绑定 yml 配置）
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.mail") // 绑定 yml 前缀
public class MailProperties {
    /**
     * 邮箱服务器地址
     */
    private String host;
    
    /**
     * 邮箱账号
     */
    private String username;
    
    /**
     * 邮箱授权码
     */
    private String password;
}
