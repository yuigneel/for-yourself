package com.yulgnier.common.config;
import cn.hutool.core.lang.Snowflake;
import com.yulgnier.common.config.properties.GateWayProperties;
import com.yulgnier.common.config.properties.JwtProperties;
import com.yulgnier.common.config.properties.SnowflakeProperties;
import com.yulgnier.common.config.properties.TruthProperties;
import com.yulgnier.common.utils.JwtUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 【仅 WebFlux / 网关 项目生效】
 * type = REACTIVE ：适配 Spring Cloud Gateway 环境
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
// 导入 网关需要的所有通用类（✅ 无任何 MVC 依赖，完美兼容）
@Import({
        RedisConfig.class, JwtProperties.class, SnowflakeProperties.class,
        JacksonConfig.class, SecurityConfig.class,SnowflakeConfig.class,
        JwtUtil.class, TruthProperties.class,
        GateWayProperties.class
})
public class ForYourselfReactiveCommonAutoConfiguration {

}
