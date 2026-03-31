package com.yulgnier.center.common.user;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@Slf4j
@MapperScan("com.yulgnier.center.common.user.mapper")
@SpringBootApplication
public class CenterCommonUserApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(CenterCommonUserApplication.class, args);
        log.info("【【【【【【 中心-用户-服务启动成功 】】】】】】");
    }
}
