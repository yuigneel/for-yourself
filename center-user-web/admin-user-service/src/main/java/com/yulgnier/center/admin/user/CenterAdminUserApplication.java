package com.yulgnier.center.admin.user;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;

@Slf4j
@MapperScan("com.yulgnier.center.admin.user.mapper")
@SpringBootApplication
@EnableFeignClients(basePackages = "com.yulgnier.center.user.api.client")
public class CenterAdminUserApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(CenterAdminUserApplication.class, args);
        log.info("==================center-admin-user-service启动成功=======================");
    }
}
