package com.yulgnier.common.utils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

/**
 * Redis 通用工具类
 * 静态调用：RedisUtil.set(...)、RedisUtil.get(...)
 */
@Component
public class RedisUtil {

    /**
     * @ Resource：从Spring容器中注入StringRedisTemplate（Redis操作核心对象）
     * 作用等价于@Autowired，是Java官方标准注入注解
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 静态持有 RedisTemplate，方便静态方法调用
     */
    private static StringRedisTemplate redisTemplate;

    /**
     * @ PostConstruct：Bean初始化后自动执行
     * 作用：将Spring注入的实例对象，赋值给静态变量，让静态方法能使用RedisTemplate
     */
    @PostConstruct
    private void init() {
        redisTemplate = this.stringRedisTemplate;
    }

    // ====================== 常用 set 方法 ======================

    /**
     * 普通存值（永久有效）
     */
    public static void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 存值 + 指定过期时间（自定义单位：秒、分、时、天）
     * 例：set("key", "val", 5, TimeUnit.MINUTES)
     */
    public static void set(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }


    // ====================== 常用 get 方法 ======================

    /**
     * 获取值，获取失败返回 null
     */
    public static String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // ====================== 常用工具方法 ======================

    /**
     * 删除key
     */
    public static Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 判断key是否存在
     */
    public static Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 给已有 key 设置过期时间（秒）
     */
    public static Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取 key 的剩余存活时间
     * @param key Redis 键名
     * @param unit 时间单位（如：TimeUnit.SECONDS、TimeUnit.MINUTES 等）
     * @return 剩余时间（指定单位）；如果 key 不存在、已过期或永久有效，返回 0
     */
    public static Long getTtl(String key, TimeUnit unit) {
        Long ttl = redisTemplate.getExpire(key, unit);
        return ttl == null || ttl < 0 ? 0 : ttl;
    }
}