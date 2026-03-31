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

@Configuration
@ConditionalOnClass(RedisConnectionFactory.class)   // 当容器中没有这个Bean时，才创建这个Bean
public class RedisConfig {

    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class)
    @ConditionalOnSingleCandidate(RedisConnectionFactory.class)//既能告诉 Spring 只有 Redis 连接工厂存在时才创建这个 Bean，又能让 IDEA 识别并消除报错
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
