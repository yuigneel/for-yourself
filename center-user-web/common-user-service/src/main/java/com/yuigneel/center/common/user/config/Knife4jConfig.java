package com.yuigneel.center.common.user.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.BinarySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    /**
     * 1. 全局OpenAPI文档基础配置【核心Bean】
     * 配置内容：文档标题、版本、开发者信息、接口描述
     * 作用：定义整个服务的API文档元信息（所有分组共用）
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("common-user-service API接口文档")       // 文档大标题（服务名+文档用途）
                        .version("v0.0.1")                  // 接口文档版本（遵循语义化版本）
                        .description("整个'ForYourself'生态的普通用户登录界面接口文档")     // 文档描述（写清服务作用、对接说明）
                        .contact(new Contact()
                                .name("yuigneel")          // 开发者/团队名称
                                .url("https://amis-homepage-main.vercel.app")  // 开发者地址（选填）
                                .email("yuigneel@outlook.com")           // 联系邮箱
                        )
                        // 服务条款地址（可选，标注API的使用规则/协议，无特殊要求可填示例地址）
                        .termsOfService("http://doc.xiaominfo.com")
                        // 配置API的许可证信息（声明项目遵循的开源协议/使用许可）
                        .license(new License()
                                .name("Apache 2.0")  // 许可证名称（如 Apache 2.0、MIT、私有协议等）
                                .url("http://doc.xiaominfo.com"))   // 许可证官方链接（建议填对应协议的官方地址）
                );
    }

    /**
     * 2. 接口分组配置【业务分组】
     * 企业规范：一个微服务/一个模块 定义一个分组
     * pathsToMatch：匹配Controller接口路径（支持通配符）
     * group：分组名称（Knife4j页面左侧展示）
     */
    @Bean
    public GroupedOpenApi businessApi() {
        return GroupedOpenApi.builder()
                .group("根普通用户接口")       // 分组名称（页面显示用）
                .pathsToMatch(      // 接口路径匹配规则 /* 表示匹配所有单级路径 /** 表示匹配所有多级路径
                        "/center-common/user/**"
                )
                .build();
    }
}