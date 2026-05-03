package com.yuigneel.center.admin.user;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@MapperScan("com.yuigneel.center.admin.user.mapper")
@SpringBootApplication
@EnableFeignClients(basePackages = "com.yuigneel.center.user.api.client")
@EnableScheduling // 开启定时任务支持
public class CenterAdminUserApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(CenterAdminUserApplication.class, args);
        log.info("==================center-admin-user-service启动成功=======================");
    }
}
