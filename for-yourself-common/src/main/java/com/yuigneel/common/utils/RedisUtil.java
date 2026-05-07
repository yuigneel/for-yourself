/*
 * For Yourself - A graduation project by the author, serving as a demonstration for the future complete project ecosystem
 * Copyright (C) 2026  Yu·Igneel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.yuigneel.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 通用工具类
 * 标准 Spring Bean 实现，通过依赖注入使用
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate stringRedisTemplate;

    // ====================== 常用 set 方法 ======================

    /**
     * 普通存值（永久有效）
     *
     * @param key   Redis键
     * @param value Redis值
     */
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 存值 + 指定过期时间（自定义单位：秒、分、时、天）
     * 例：redisUtil.set("key", "val", 5, TimeUnit.MINUTES)
     *
     * @param key     Redis键
     * @param value   Redis值
     * @param timeout 过期时间数值
     * @param unit    时间单位
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }


    // ====================== 常用 get 方法 ======================

    /**
     * 获取值，获取失败返回 null
     *
     * @param key Redis键
     * @return Redis值，不存在返回null
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    // ====================== 常用工具方法 ======================

    /**
     * 删除key
     *
     * @param key Redis键
     * @return 是否删除成功
     */
    public Boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }

    /**
     * 判断key是否存在
     *
     * @param key Redis键
     * @return 是否存在
     */
    public Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    /**
     * 给已有 key 设置过期时间
     *
     * @param key     Redis键
     * @param timeout 过期时间数值
     * @param unit    时间单位
     * @return 是否设置成功
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return stringRedisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取 key 的剩余存活时间
     *
     * @param key  Redis 键名
     * @param unit 时间单位（如：TimeUnit.SECONDS、TimeUnit.MINUTES 等）
     * @return 剩余时间（指定单位）；如果 key 不存在、已过期或永久有效，返回 0
     */
    public Long getTtl(String key, TimeUnit unit) {
        /*
        如果 key 不存在或已过期，返回 -2；
        如果 key 存在但没有设置过期时间（永久有效），返回 -1；
        如果 key 存在且有过期时间，返回大于 0 的整数，代表剩余时间（单位由 unit 指定）。
        如果 key 存在但不足1个单位，返回 0。
         */
        return stringRedisTemplate.getExpire(key, unit);
    }
}