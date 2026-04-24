package com.yulgnier.common.utils;

import com.yulgnier.common.config.properties.MinIOProperties;
import com.yulgnier.common.exception.ForYourselfException;
import com.yulgnier.common.model.result.ResultCodeEnum;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 对象存储工具类
 * <p>
 * 标准 Spring 单例 Bean 实现，线程安全、可测试、职责单一
 * 上层统一全局异常捕获，方法内部仅打印日志
 * 仅支持【存储桶级别】权限控制（MinIO 不支持单文件 Policy）
 *
 * @author yulgnier
 * @date 2025
 */
@Slf4j
@Component
public class MinioUtil {

    // ===================== 注入核心依赖（Spring 单例，无静态风险） =====================
    private final MinioClient minioClient;
    private final MinIOProperties minIOProperties;

    /**
     * 默认存储桶名称（初始化校验非空）
     */
    private final String defaultBucket;

    /**
     * 构造器初始化 + 全局配置合法性校验
     */
    public MinioUtil(MinioClient minioClient, MinIOProperties minIOProperties) {
        this.minioClient = minioClient;
        this.minIOProperties = minIOProperties;
        this.defaultBucket = minIOProperties.getBucketName();
        // 启动时校验核心配置，提前暴露问题
        checkCoreConfig();
    }

    // ================================ 存储桶管理模块 ================================

    /**
     * 查询所有存储桶名称
     */
    public List<String> listAllBuckets() {
        List<String> bucketNames = new ArrayList<>();
        try {
            List<Bucket> buckets = minioClient.listBuckets();
            for (Bucket bucket : buckets) {
                bucketNames.add(bucket.name());
            }
            log.info("【MinIO】查询所有存储桶成功，数量：{}", bucketNames.size());
            return bucketNames;
        } catch (Exception e) {
            log.error("【MinIO】查询所有存储桶失败", e);
            throw new ForYourselfException(ResultCodeEnum.MINIO_SERVICE_ERROR, e);
        }
    }

    /**
     * 判断存储桶是否存在
     */
    public boolean bucketExists(String bucketName) {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            log.error("【MinIO】判断存储桶失败：桶名不能为空");
            return false;
        }
        try {
            return minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName.trim()).build()
            );
        } catch (Exception e) {
            log.error("【MinIO】判断存储桶 [{}] 是否存在失败", bucketName, e);
            throw new ForYourselfException(ResultCodeEnum.MINIO_SERVICE_ERROR, e);
        }
    }

    /**
     * 批量创建私有存储桶
     */
    public void createBuckets(String... bucketNames) {
        if (bucketNames == null || bucketNames.length == 0) {
            log.error("【MinIO】创建存储桶失败：未指定桶名");
            throw new ForYourselfException(ResultCodeEnum.MINIO_SERVICE_ERROR, null);
        }
        try {
            for (String bucketName : bucketNames) {
                String targetBucket = (bucketName == null) ? "" : bucketName.trim();
                if (targetBucket.isEmpty()) {
                    log.warn("【MinIO】跳过空存储桶名称");
                    continue;
                }
                if (bucketExists(targetBucket)) {
                    log.info("【MinIO】存储桶 [{}] 已存在，无需重复创建", targetBucket);
                    continue;
                }
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(targetBucket).build());
                log.info("【MinIO】存储桶 [{}] 创建成功（默认私有）", targetBucket);
            }
        } catch (Exception e) {
            log.error("【MinIO】批量创建存储桶失败", e);
            throw new ForYourselfException(ResultCodeEnum.MINIO_SERVICE_ERROR, e);
        }
    }

    /**
     * 批量删除存储桶（自动清空文件）
     */
    public void deleteBuckets(String... bucketNames) {
        if (bucketNames == null || bucketNames.length == 0) {
            log.error("【MinIO】删除存储桶失败：未指定桶名");
            throw new ForYourselfException(ResultCodeEnum.MINIO_SERVICE_ERROR, null);
        }
        try {
            for (String bucketName : bucketNames) {
                String targetBucket = (bucketName == null) ? "" : bucketName.trim();
                if (targetBucket.isEmpty()) {
                    log.warn("【MinIO】跳过空存储桶名称");
                    continue;
                }
                if (!bucketExists(targetBucket)) {
                    log.info("【MinIO】存储桶 [{}] 不存在，无需删除", targetBucket);
                    continue;
                }

                // 清空文件
                Iterable<Result<Item>> items = minioClient.listObjects(
                        ListObjectsArgs.builder().bucket(targetBucket).recursive(true).build()
                );
                for (Result<Item> item : items) {
                    minioClient.removeObject(
                            RemoveObjectArgs.builder().bucket(targetBucket).object(item.get().objectName()).build()
                    );
                }

                // 删除桶
                minioClient.removeBucket(RemoveBucketArgs.builder().bucket(targetBucket).build());
                log.info("【MinIO】存储桶 [{}] 已清空并删除成功", targetBucket);
            }
        } catch (Exception e) {
            log.error("【MinIO】批量删除存储桶失败", e);
            throw new ForYourselfException(ResultCodeEnum.MINIO_SERVICE_ERROR, e);
        }
    }

    // ================================ 文件操作模块 ================================

    /**
     * 文件上传（自动生成唯一文件名）
     *
     * @param file       待上传文件
     * @param bucketName 可选，指定存储桶名称，不传则使用默认桶
     * @return 上传后的唯一文件名
     * @throws ForYourselfException 当文件为空、参数非法或上传失败时抛出
     */
    public String uploadFile(MultipartFile file, String... bucketName) {
        if (file == null || file.isEmpty()) {
            log.error("【MinIO】文件上传失败：文件为空");
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, "上传文件不能为空");
        }

        String targetBucket = getTargetBucket(bucketName);
        if (targetBucket == null) {
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, "存储桶名称无效");
        }

        String originalFileName = file.getOriginalFilename() == null ? "unknown-file" : file.getOriginalFilename();
        // 安全检查：防止路径遍历攻击
        if (originalFileName.contains("..") || originalFileName.contains("/") || originalFileName.contains("\\")) {
            log.error("【MinIO】文件上传失败：文件名包含非法字符 | 原始文件名：{}", originalFileName);
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, "文件名包含非法字符");
        }

        String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + "-" + originalFileName;

        try (InputStream inputStream = file.getInputStream()) {
            if (!bucketExists(targetBucket)) {
                createBuckets(targetBucket);
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(targetBucket)
                            .object(uniqueFileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.info("【MinIO】文件上传成功 | 存储桶：{} | 原始文件名：{} | 唯一文件名：{} | 文件大小：{} bytes | 文件类型：{}",
                    targetBucket, originalFileName, uniqueFileName, file.getSize(), file.getContentType());
            return uniqueFileName;
        } catch (ForYourselfException e) {
            throw e;
        } catch (Exception e) {
            log.error("【MinIO】文件上传失败 | 存储桶：{} | 原始文件名：{} | 唯一文件名：{} | 文件大小：{} bytes",
                    targetBucket, originalFileName, uniqueFileName, file.getSize(), e);
            throw new ForYourselfException(ResultCodeEnum.MINIO_SERVICE_ERROR, "文件上传失败，请稍后重试");
        }
    }

    /**
     * 批量删除文件（使用MinIO批量删除API，提高性能）
     *
     * @param uniqueFileNames 待删除的唯一文件名集合
     * @param bucketName      可选，指定存储桶名称，不传则使用默认桶
     * @return 是否全部删除成功
     * @throws ForYourselfException 当参数非法或删除失败时抛出
     */
    public boolean deleteObjects(Set<String> uniqueFileNames, String... bucketName) {
        if (uniqueFileNames == null || uniqueFileNames.isEmpty()) {
            log.error("【MinIO】批量删除文件失败：文件列表为空");
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, "待删除文件列表不能为空");
        }

        String targetBucket = getTargetBucket(bucketName);
        if (targetBucket == null) {
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, "存储桶名称无效");
        }

        if (!bucketExists(targetBucket)) {
            log.warn("【MinIO】存储桶 [{}] 不存在，无需删除", targetBucket);
            return false;
        }

        List<String> failedFiles = new ArrayList<>();
        try {
            for (String fileName : uniqueFileNames) {
                String targetFile = (fileName == null) ? "" : fileName.trim();
                if (targetFile.isEmpty()) {
                    log.warn("【MinIO】跳过空文件名");
                    failedFiles.add(fileName);
                    continue;
                }
                try {
                    minioClient.removeObject(
                            RemoveObjectArgs.builder().bucket(targetBucket).object(targetFile).build()
                    );
                    log.info("【MinIO】文件删除成功 | 存储桶：{} | 文件名：{}", targetBucket, targetFile);
                } catch (Exception e) {
                    log.error("【MinIO】单个文件删除失败 | 存储桶：{} | 文件名：{}", targetBucket, targetFile, e);
                    failedFiles.add(targetFile);
                }
            }
        } catch (Exception e) {
            log.error("【MinIO】批量删除文件异常", e);
            throw new ForYourselfException(ResultCodeEnum.MINIO_SERVICE_ERROR, "批量删除文件失败");
        }

        if (!failedFiles.isEmpty()) {
            log.warn("【MinIO】部分文件删除失败 | 失败数量：{} | 失败文件：{}", failedFiles.size(), failedFiles);
            return false;
        }
        return true;
    }

    // ================================ URL 生成模块 ================================

    /**
     * 生成临时预签名 URL（推荐使用此重载方法）
     *
     * @param uniqueFileName 文件唯一名称
     * @param expireSeconds  过期时间（秒）
     * @param bucketName     可选，指定存储桶名称，不传则使用默认桶
     * @return 预签名URL
     * @throws ForYourselfException 当参数非法或生成失败时抛出
     */
    public String getPresignedUrl(String uniqueFileName, long expireSeconds, String... bucketName) {
        return getPresignedUrl(uniqueFileName, (int) expireSeconds, TimeUnit.SECONDS, bucketName);
    }

    /**
     * 生成临时预签名 URL
     *
     * @param uniqueFileName 文件唯一名称
     * @param expireTime     过期时间数值
     * @param timeUnit       时间单位
     * @param bucketName     可选，指定存储桶名称，不传则使用默认桶
     * @return 预签名URL
     * @throws ForYourselfException 当参数非法或生成失败时抛出
     */
    public String getPresignedUrl(String uniqueFileName, int expireTime, TimeUnit timeUnit, String... bucketName) {
        if (uniqueFileName == null || uniqueFileName.trim().isEmpty() || expireTime <= 0 || timeUnit == null) {
            log.error("【MinIO】生成预签名URL失败：参数非法");
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, "生成预签名URL参数非法");
        }

        String targetBucket = getTargetBucket(bucketName);
        if (targetBucket == null) {
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, "存储桶名称无效");
        }

        if (!bucketExists(targetBucket)) {
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, "存储桶不存在");
        }

        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(targetBucket).object(uniqueFileName).build());
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(targetBucket)
                            .object(uniqueFileName)
                            .expiry(expireTime, timeUnit)
                            .build()
            );
            log.info("【MinIO】生成预签名URL成功 | 文件名：{} | 过期时间：{} {}", uniqueFileName, expireTime, timeUnit);
            return url;
        } catch (ForYourselfException e) {
            throw e;
        } catch (Exception e) {
            log.error("【MinIO】生成预签名URL失败 | 文件名：{}", uniqueFileName, e);
            throw new ForYourselfException(ResultCodeEnum.MINIO_SERVICE_ERROR, "生成预签名URL失败");
        }
    }

    /**
     * 生成公共访问 URL（自动进行URL编码）
     *
     * @param uniqueFileName 文件唯一名称
     * @param bucketName     可选，指定存储桶名称，不传则使用默认桶
     * @return 公共访问URL
     * @throws ForYourselfException 当参数非法或生成失败时抛出
     */
    public String getPublicUrl(String uniqueFileName, String... bucketName) {
        if (uniqueFileName == null || uniqueFileName.trim().isEmpty()) {
            log.error("【MinIO】生成公共URL失败：文件名为空");
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, "文件名不能为空");
        }

        String targetBucket = getTargetBucket(bucketName);
        if (targetBucket == null) {
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, "存储桶名称无效");
        }

        if (!bucketExists(targetBucket)) {
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, "存储桶不存在");
        }

        try {
            String endpoint = minIOProperties.getEndpoint();
            if (endpoint.endsWith("/")) {
                endpoint = endpoint.substring(0, endpoint.length() - 1);
            }
            // URL编码文件名，处理中文、空格等特殊字符
            String encodedFileName = URLEncoder.encode(uniqueFileName.trim(), StandardCharsets.UTF_8)
                    .replace("+", "%20"); // URLEncoder会将空格编码为+，需要替换为%20
            String publicUrl = endpoint + "/" + targetBucket + "/" + encodedFileName;
            log.info("【MinIO】生成公共URL成功：{}", publicUrl);
            return publicUrl;
        } catch (ForYourselfException e) {
            throw e;
        } catch (Exception e) {
            log.error("【MinIO】生成公共URL失败 | 文件名：{}", uniqueFileName, e);
            throw new ForYourselfException(ResultCodeEnum.MINIO_SERVICE_ERROR, "生成公共URL失败");
        }
    }

    // ================================ 权限配置模块 ================================

    /**
     * 设置存储桶权限 public/private
     *
     * @param bucketName 存储桶名称
     * @param permission 权限类型：public 或 private
     * @return 是否设置成功
     * @throws ForYourselfException 当参数非法或设置失败时抛出
     */
    public boolean setBucketPermission(String bucketName, String permission) {
        if (permission == null || !("public".equalsIgnoreCase(permission.trim()) || "private".equalsIgnoreCase(permission.trim()))) {
            log.error("【MinIO】设置权限失败：仅支持 public/private");
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, "权限类型仅支持 public 或 private");
        }

        String targetBucket = getTargetBucket(bucketName);
        if (targetBucket == null) {
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, "存储桶名称无效");
        }

        if (!bucketExists(targetBucket)) {
            throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, "存储桶不存在");
        }

        try {
            String policy = buildBucketPolicy(targetBucket, permission.trim().toLowerCase());
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(targetBucket).config(policy).build());
            log.info("【MinIO】存储桶 [{}] 权限设置成功：{}", targetBucket, permission);
            return true;
        } catch (ForYourselfException e) {
            throw e;
        } catch (Exception e) {
            log.error("【MinIO】设置存储桶 [{}] 权限失败", targetBucket, e);
            throw new ForYourselfException(ResultCodeEnum.MINIO_SERVICE_ERROR, "设置存储桶权限失败");
        }
    }

    // ================================ 私有辅助方法 ================================

    /**
     * 核心配置校验（项目启动时校验，避免运行时空指针）
     */
    private void checkCoreConfig() {
        if (minioClient == null) {
            throw new IllegalStateException("【MinIO】初始化失败：MinioClient 未注入");
        }
        if (minIOProperties == null) {
            throw new IllegalStateException("【MinIO】初始化失败：MinIOProperties 未注入");
        }
        if (defaultBucket == null || defaultBucket.trim().isEmpty()) {
            throw new IllegalStateException("【MinIO】初始化失败：配置文件中 bucketName 不能为空");
        }
        log.info("【MinIO】核心配置校验通过，默认存储桶：{}", defaultBucket);
    }

    /**
     * 构建桶权限策略
     */
    private String buildBucketPolicy(String bucketName, String permission) {
        if ("public".equals(permission)) {
            return """
                    {
                      "Version": "2012-10-17",
                      "Statement": [
                        {
                          "Effect": "Allow",
                          "Principal": "*",
                          "Action": ["s3:GetObject"],
                          "Resource": ["arn:aws:s3:::%s/*"]
                        }
                      ]
                    }""".formatted(bucketName);
        } else {
            return """
                    {
                      "Version": "2012-10-17",
                      "Statement": []
                    }""";
        }
    }

    /**
     * 获取目标存储桶（优先传入，兜底默认桶）
     */
    private String getTargetBucket(String... bucketName) {
        String targetBucket = (bucketName != null && bucketName.length > 0
                && bucketName[0] != null && !bucketName[0].trim().isEmpty())
                ? bucketName[0].trim()
                : defaultBucket;

        if (targetBucket == null || targetBucket.trim().isEmpty()) {
            log.error("【MinIO】目标存储桶名称为空");
            return null;
        }
        return targetBucket;
    }
}