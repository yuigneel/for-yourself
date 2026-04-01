package com.yulgnier.common.utils;

import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

/**
 * Cloudflare Turnstile 人机验证工具类
 * 静态调用，无需 Spring 注入/扫描
 */
public final class CloudflareTurnstileUtils {

    // Cloudflare 官方验证地址
    private static final String CF_VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    // 工具类禁止实例化
    private CloudflareTurnstileUtils() {}

    /**
     * 验证 Turnstile 令牌
     * @param clientToken 前端传来的 cf-turnstile-response
     * @param serverSecret 后端的 SecretKey
     * @return 验证成功 true / 失败 false
     */
    public static boolean verify(String clientToken, String serverSecret) {
        try {
            // 1. 判空
            if (clientToken == null || clientToken.isBlank() || serverSecret == null || serverSecret.isBlank()) {
                return false;
            }

            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> params = new HashMap<>(2);
            params.put("secret", serverSecret);
            params.put("response", clientToken);

            // 2. 调用 Cloudflare 验证接口
            Map<String, Object> result = restTemplate.postForObject(CF_VERIFY_URL, params, Map.class);

            // 3. 返回验证结果
            return Boolean.TRUE.equals(result.get("success"));
        } catch (Exception e) {
            // 网络异常/接口异常都算验证失败
            return false;
        }
    }
}
