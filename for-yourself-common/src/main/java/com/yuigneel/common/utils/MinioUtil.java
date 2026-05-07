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
package com.yuigneel.common.utils;

import com.yuigneel.common.config.properties.MinIOProperties;
import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 对象存储工具类
 * <p>
 * 简化版 MinIO 工具类，专注于核心文件操作功能。
 * 所有操作均使用配置文件中定义的默认存储桶，桶权限默认为私有。
 * Bean 初始化时会自动检查并创建默认桶（如果不存在）。
 * <p>
 * 核心特性：
 * - 自动初始化：Spring 容器注入后自动确保默认桶存在且为私有
 * - 简化接口：仅提供上传、删除、存在性判断、临时URL生成四个核心方法
 * - 异常处理：所有方法直接抛出原始异常，由调用方统一处理
 * - 安全设计：默认桶为私有，通过预签名URL访问
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 上传文件
 * String fileName = minioUtil.uploadFile(multipartFile);
 * 
 * // 判断文件是否存在
 * boolean exists = minioUtil.isFileExists(fileName);
 * 
 * // 获取临时访问URL（7天有效期）
 * String url = minioUtil.getPresignedUrl(fileName, 7, TimeUnit.DAYS);
 * 
 * // 删除文件
 * boolean deleted = minioUtil.deleteFile(fileName);
 * }</pre>
 *
 * @author yulgnier
 * @date 2025
 */
@Slf4j
@Component
public class MinioUtil {

    /**
     * MinIO 客户端实例
     */
    private final MinioClient minioClient;

    /**
     * MinIO 配置属性
     */
    private final MinIOProperties minIOProperties;

    /**
     * 默认存储桶名称（从配置文件读取）
     */
    private final String defaultBucket;

    /**
     * 预签名URL最大过期时间（秒）
     * MinIO 限制：最大7天（604800秒）
     */
    private static final long MAX_EXPIRY_SECONDS = 7 * 24 * 60 * 60;

    /**
     * 预签名URL最小过期时间（秒）
     * MinIO 限制：最小1秒
     */
    private static final long MIN_EXPIRY_SECONDS = 1;

    /**
     * 构造函数：注入依赖并自动初始化默认桶
     *
     * @param minioClient      MinIO 客户端
     * @param minIOProperties  MinIO 配置属性
     */
    public MinioUtil(MinioClient minioClient, MinIOProperties minIOProperties) {
        this.minioClient = minioClient;
        this.minIOProperties = minIOProperties;
        this.defaultBucket = minIOProperties.getBucketName();

        // 验证核心配置
        validateConfig();

        // 自动初始化默认桶（确保桶存在且为私有）
        initializeDefaultBucket();
    }

    /**
     * 验证核心配置项是否完整
     *
     * @throws IllegalStateException 当配置项缺失时抛出异常
     */
    private void validateConfig() {
        if (minioClient == null) {
            throw new IllegalStateException("MinioClient 未正确初始化");
        }
        if (minIOProperties == null) {
            throw new IllegalStateException("MinIOProperties 未正确注入");
        }
        if (defaultBucket == null || defaultBucket.trim().isEmpty()) {
            throw new IllegalStateException("配置文件中 minio.bucket-name 不能为空");
        }
        log.info("MinIO 配置验证通过，默认存储桶: {}", defaultBucket);
    }

    /**
     * 初始化默认存储桶
     * <p>
     * 检查默认桶是否存在，不存在则创建并设置为私有权限。
     * 此方法在 Bean 构造时自动执行，确保后续操作无需关心桶的存在性。
     */
    private void initializeDefaultBucket() {
        try {
            // 检查桶是否存在
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(defaultBucket).build()
            );

            if (!bucketExists) {
                // 创建新桶（MinIO 创建的桶默认为私有）
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(defaultBucket).build()
                );
                log.info("默认存储桶 [{}] 创建成功（私有权限）", defaultBucket);
            } else {
                log.info("默认存储桶 [{}] 已存在", defaultBucket);
            }
        } catch (Exception e) {
            log.error("初始化默认存储桶 [{}] 失败", defaultBucket, e);
            throw new IllegalStateException("无法初始化默认存储桶: " + defaultBucket, e);
        }
    }

    /**
     * 上传文件到 MinIO
     * <p>
     * 将上传的文件保存到默认存储桶中，自动生成唯一的文件名。
     * 文件名格式：UUID（无横杠）- 原始文件名
     * <p>
     * 注意：返回的文件名长度不超过 VARCHAR(512)，可直接存入数据库。
     *
     * @param file 待上传的文件，不能为 null 或空
     * @return 文件唯一标识符（文件名），可用于后续的删除、查询、生成URL等操作
     * @throws Exception 当文件为空、参数非法或上传失败时抛出原始异常
     */
    public String uploadFile(MultipartFile file) throws Exception {
        // 参数校验
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            originalFileName = "unknown-file";
        }

        // 安全检查：防止路径遍历攻击
        if (originalFileName.contains("..") || originalFileName.contains("/") || originalFileName.contains("\\")) {
            throw new IllegalArgumentException("文件名包含非法字符");
        }

        // 生成唯一文件名：UUID + 原始文件名
        String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + "-" + originalFileName;

        // 确保文件名不超过512字符（预留一些空间给可能的路径前缀）
        if (uniqueFileName.length() > 500) {
            // 截取原始文件名部分，保留扩展名
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex > 0) {
                String extension = originalFileName.substring(dotIndex);
                String nameWithoutExt = originalFileName.substring(0, dotIndex);
                int maxNameLength = 500 - 33 - extension.length(); // 33是UUID长度+横杠
                if (maxNameLength > 0) {
                    nameWithoutExt = nameWithoutExt.substring(0, Math.min(nameWithoutExt.length(), maxNameLength));
                    uniqueFileName = UUID.randomUUID().toString().replace("-", "") + "-" + nameWithoutExt + extension;
                }
            }
        }

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(defaultBucket)
                            .object(uniqueFileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("文件上传成功 | 存储桶: {} | 原始文件名: {} | 唯一文件名: {} | 大小: {} bytes",
                    defaultBucket, originalFileName, uniqueFileName, file.getSize());

            return uniqueFileName;
        }
    }

    /**
     * 删除文件
     * <p>
     * 从默认存储桶中删除指定文件。
     *
     * @param uniqueFileName 文件唯一标识符（由 uploadFile 方法返回）
     * @return true 表示删除成功，false 表示文件不存在或删除失败
     */
    public boolean deleteFile(String uniqueFileName) {
        // 参数校验
        if (uniqueFileName == null || uniqueFileName.trim().isEmpty()) {
            log.warn("删除文件失败：文件名为空");
            return false;
        }

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(defaultBucket)
                            .object(uniqueFileName.trim())
                            .build()
            );

            log.info("文件删除成功 | 存储桶: {} | 文件名: {}", defaultBucket, uniqueFileName);
            return true;
        } catch (Exception e) {
            log.error("文件删除失败 | 存储桶: {} | 文件名: {}", defaultBucket, uniqueFileName, e);
            return false;
        }
    }

    /**
     * 判断文件是否存在
     * <p>
     * 检查指定文件是否存在于默认存储桶中。
     *
     * @param uniqueFileName 文件唯一标识符（由 uploadFile 方法返回）
     * @return true 表示文件存在，false 表示文件不存在或查询失败
     */
    public boolean isFileExists(String uniqueFileName) {
        // 参数校验
        if (uniqueFileName == null || uniqueFileName.trim().isEmpty()) {
            return false;
        }

        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(defaultBucket)
                            .object(uniqueFileName.trim())
                            .build()
            );
            return true;
        } catch (Exception e) {
            // statObject 在文件不存在时会抛出异常
            return false;
        }
    }

    /**
     * 生成文件的临时预签名访问URL
     * <p>
     * 为私有存储桶中的文件生成一个临时可访问的URL。
     * URL 有效期根据传入的时间值和时间单位计算，支持边界值自动调整：
     * - 时间值为 0 或负数：返回最大有效期的URL（7天）
     * - 计算结果超过最大值：按最大值（7天）处理
     * - 计算结果小于最小值：按最小值（1秒）处理
     *
     * @param uniqueFileName 文件唯一标识符（由 uploadFile 方法返回）
     * @param expireTime     过期时间数值
     * @param timeUnit       时间单位（如 TimeUnit.SECONDS、TimeUnit.DAYS 等）
     * @return 预签名URL字符串，可直接用于浏览器访问或前端展示
     * @throws Exception 当参数非法或生成 URL 失败时抛出原始异常
     */
    public String getPresignedUrl(String uniqueFileName, long expireTime, TimeUnit timeUnit) throws Exception {
        // 参数校验
        if (uniqueFileName == null || uniqueFileName.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        if (timeUnit == null) {
            throw new IllegalArgumentException("时间单位不能为空");
        }

        // 计算过期时间（秒）
        long expireSeconds;
        if (expireTime <= 0) {
            // 时间值为0或负数，使用最大值
            expireSeconds = MAX_EXPIRY_SECONDS;
            log.debug("过期时间为0或负数，使用最大有效期: {} 秒", expireSeconds);
        } else {
            expireSeconds = timeUnit.toSeconds(expireTime);
        }

        // 边界值处理
        if (expireSeconds > MAX_EXPIRY_SECONDS) {
            expireSeconds = MAX_EXPIRY_SECONDS;
            log.debug("过期时间超过最大值，调整为: {} 秒（7天）", expireSeconds);
        } else if (expireSeconds < MIN_EXPIRY_SECONDS) {
            expireSeconds = MIN_EXPIRY_SECONDS;
            log.debug("过期时间小于最小值，调整为: {} 秒", expireSeconds);
        }

        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(defaultBucket)
                            .object(uniqueFileName.trim())
                            .expiry((int) expireSeconds, TimeUnit.SECONDS)
                            .build()
            );

            log.info("生成预签名URL成功 | 文件名: {} | 有效期: {} 秒", uniqueFileName, expireSeconds);
            return url;
        } catch (Exception e) {
            log.error("生成预签名URL失败 | 文件名: {}", uniqueFileName, e);
            throw e;
        }
    }
}