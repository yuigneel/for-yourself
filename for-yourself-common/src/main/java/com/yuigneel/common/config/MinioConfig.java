/*
 * For Yourself - A graduation project by the author, serving as a demonstration for the future complete project ecosystem
 * Copyright (C) 2026  Yu·Igneel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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