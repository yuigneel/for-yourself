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
        log.info("==================center-common-user-service启动成功=======================");
    }
    /*
        荣耀的梦幻：我要开源，分享我的点子，分享快乐
                                        开源是把双刃剑：哥
                                开源不是目的，而是手段。：哥
        荣耀的梦幻：。。。
     */
}