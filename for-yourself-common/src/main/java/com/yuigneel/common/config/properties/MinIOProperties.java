package com.yuigneel.common.config.properties;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "minio")
public class MinIOProperties {

    // 基础认证配置
    private String accessKey;    // MinIO账号
    private String secretKey;    // MinIO密码
    private String bucketName;   // 默认存储桶名称

    // 补充yml里的配置字段（必须加，否则配置类拿不到这些值）
    private String endpoint;     // MinIO服务API地址（http://127.0.0.1:9000）
    private Boolean secure;      // 是否启用HTTPS
    private Boolean pathStyleAccess; // 是否启用路径样式访问

    // 手动添加getter/setter方法
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public Boolean getSecure() {
        return secure;
    }

    public void setSecure(Boolean secure) {
        this.secure = secure;
    }

    public Boolean getPathStyleAccess() {
        return pathStyleAccess;
    }

    public void setPathStyleAccess(Boolean pathStyleAccess) {
        this.pathStyleAccess = pathStyleAccess;
    }

//
//    @PostConstruct      // 临时调试方法：Bean初始化后执行的方法，验证是否被Spring扫描到
//    public void init() {    // 打印关键信息：确认Bean被加载，同时可查看配置是否注入成功
//        log.debug("======= MinIOProperties 被Spring扫描并初始化 =======");
//        log.debug("accessKey = " + this.accessKey); // 打印配置值（yml里配了就有值，没配则为null）
//        log.debug("secretKey = " + this.secretKey);
//        log.debug("bucketName = " + this.bucketName);
//        log.debug("==================================================");
//    }

}
