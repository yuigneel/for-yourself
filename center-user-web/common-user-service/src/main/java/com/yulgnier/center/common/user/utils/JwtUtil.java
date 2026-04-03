package com.yulgnier.center.common.user.utils;

import cn.hutool.core.lang.Snowflake;
import com.yulgnier.center.common.user.config.properties.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JWT工具类（静态调用版，和RedisUtil逻辑完全一致）
 * 调用方式：JwtUtil.generateToken(...)
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {

    // ====================== 1. 非静态变量：接收 Spring 注入（不能是static） ======================
    // 构造器注入（Spring官方推荐）
    private final JwtProperties injectJwtProperties;
    private final Snowflake injectSnowflake;

    // ====================== 2. 静态变量：供静态方法使用 ======================
    private static JwtProperties jwtProperties;
    private static Snowflake snowflake;

    // ====================== 3. @PostConstruct 方法：赋值！（只能加在方法上） ======================
    @PostConstruct
    private void init() {
        // 把Spring注入的对象 → 赋值给静态变量
        jwtProperties = this.injectJwtProperties;
        snowflake = this.injectSnowflake;
    }

    // ====================== 核心公共静态方法 ======================

    /**
     * 生成 JWT令牌
     */
    public static String generateToken(Map<String, Object> claims, long expire, TimeUnit timeUnit) {
        String jti = snowflake.nextIdStr();
        Date now = new Date();
        long expireMillis = timeUnit.toMillis(expire);
        Date expiration = new Date(now.getTime() + expireMillis);
        SecretKey secretKey = getSecretKey();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .setId(jti)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证 JWT令牌
     */
    public static boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取 Token载荷
     */
    public static Map<String, Object> getClaimsFromToken(String token) {
        if (!validateToken(token)) {
            return null;
        }
        return parseClaims(token);
    }

    // ====================== 私有工具方法 ======================
    private static SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
    }

    private static Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}