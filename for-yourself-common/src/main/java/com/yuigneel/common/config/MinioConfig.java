package com.yuigneel.common.config;

import com.yuigneel.common.config.properties.MinIOProperties;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO客户端配置类
 * 作用：根据MinIOProperties的配置，创建MinioClient对象并交给Spring容器管理
 * 工具类可直接@Autowired注入MinioClient使用
 * 适配MinIO Java客户端8.x所有版本（解决secure/pathStyleAccess方法无法解析问题）
 */
@Configuration
public class MinioConfig {

    @Autowired
    private MinIOProperties minIOProperties;

    /**
     * 创建MinioClient Bean（核心）
     * @return 配置好的MinIO客户端对象
     */
    @Bean
    public MinioClient minioClient() {
        // 核心修复：移除不兼容的secure()/pathStyleAccess()方法，改用极简构建方式
        // 说明：
        // 1. HTTPS（secure）：只要endpoint以https://开头，MinIO会自动启用HTTPS，无需手动配置
        // 2. 路径样式访问：MinIO 8.x默认是虚拟主机模式，无需手动配置；若需兼容旧模式，仅需确保endpoint格式正确即可
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minIOProperties.getEndpoint()) // API地址（http/https开头自动适配secure）
                .credentials(minIOProperties.getAccessKey(), minIOProperties.getSecretKey()) // 账号密码
                .build();

        return minioClient;
    }
}