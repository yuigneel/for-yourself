package com.yuigneel.common.utils;

import cn.hutool.core.lang.Snowflake;
import com.yuigneel.common.config.properties.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JWT工具类
 * 标准 Spring Bean 实现，通过依赖注入使用
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;
    private final Snowflake snowflake;

    // ====================== 核心公共方法 ======================

    /**
     * 生成 JWT令牌
     *
     * @param claims   JWT载荷数据
     * @param expire   过期时间数值
     * @param timeUnit 时间单位
     * @return JWT Token字符串
     */
    public String generateToken(Map<String, Object> claims, long expire, TimeUnit timeUnit) {
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
     *
     * @param token JWT Token字符串
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取 Token载荷
     *
     * @param token JWT Token字符串
     * @return 载荷Map，token无效返回null
     */
    public Map<String, Object> getClaimsFromToken(String token) {
        if (!validateToken(token)) {
            return null;
        }
        return parseClaims(token);
    }

    // ====================== 私有工具方法 ======================
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}