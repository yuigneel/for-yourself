package com.yuigneel.center.common.user;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@MapperScan("com.yuigneel.center.common.user.mapper")
@SpringBootApplication
@EnableScheduling // 开启定时任务支持
public class CenterCommonUserApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(CenterCommonUserApplication.class, args);
        log.info("==================center-common-user-service启动成功=======================");
    }
}