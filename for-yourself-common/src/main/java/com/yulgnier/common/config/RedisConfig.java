package com.yulgnier.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.swing.*;

@Configuration
@ConditionalOnClass(RedisConnectionFactory.class)   // 只有项目引入了Redis依赖（存在该类），当前配置类才生效
public class RedisConfig {

    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class)//Spring 容器里，如果【还没有 RedisTemplate 这个 Bean】，我才创建；如果【已经有别人创建了】，我就不创建、不覆盖、直接跳过
    @ConditionalOnSingleCandidate(RedisConnectionFactory.class)//既能告诉Spring只有这个 Bean 是不是只有一个才创建这个 Bean，又能让 IDEA 识别并消除报错
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        // Key 序列化：字符串    (宝宝好难ƪ(˘⌣˘)ʃ)
        template.setKeySerializer(new StringRedisSerializer());
        // Value 序列化：JSON
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        // Hash 序列化
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
