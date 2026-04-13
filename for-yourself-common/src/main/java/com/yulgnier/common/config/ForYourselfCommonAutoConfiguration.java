package com.yulgnier.common.config;

import com.yulgnier.common.config.properties.GateWayProperties;
import com.yulgnier.common.config.properties.JwtProperties;
import com.yulgnier.common.config.properties.SnowflakeProperties;
import com.yulgnier.common.config.properties.TruthProperties;
import com.yulgnier.common.exception.GlobalExceptionHandler;
import com.yulgnier.common.utils.JwtUtil;
import com.yulgnier.common.utils.RedisUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Common 通用模块 自动配置类
 * <p>
 * 【核心作用】
 * 当其他项目引入了这个通用模块时，不需要手动写 @ComponentScan 扫描包
 * SpringBoot 会自动加载这个类，自动注册全局异常处理器
 * 做到：引入依赖 → 直接使用，零配置生效
 * <p>
 * 【解决的问题】
 * 避免其他项目不知道包路径，无法扫描到 GlobalExceptionHandler 的问题
 * <p>
 * 【使用方式】
 * 其他项目只需要在 pom.xml 引入通用模块依赖即可，无需任何额外配置
 */
@Configuration       // 标记这是一个Spring配置类，Spring会识别并加载
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
// 限定：只有在 SpringMVC Web 项目中才生效（非Web项目不加载，避免无效配置）
@Import({GlobalExceptionHandler.class, RedisConfig.class, RedisUtil.class, JacksonConfig.class, SecurityConfig.class,
        JwtProperties.class, SnowflakeProperties.class, SnowflakeConfig.class, JwtUtil.class, TruthProperties.class,
        WebMVCConfig.class, GateWayProperties.class,DefaultFeignClient.class
})   // 核心：自动导入全局异常处理器，将其注册到Spring容器中
public class ForYourselfCommonAutoConfiguration {
    /**
     *
     * 本类不需要写任何业务代码
     * 所有功能完全依靠上面的注解实现自动配置
     * 未来如果需要添加拦截器、工具类等自动配置，都可以在这个类里扩展
     */
}