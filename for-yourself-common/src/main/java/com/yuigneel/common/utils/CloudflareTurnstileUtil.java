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

import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

/**
 * Cloudflare Turnstile 人机验证工具类
 * 静态调用，无需 Spring 注入/扫描
 */
public final class CloudflareTurnstileUtil {

    // Cloudflare 官方验证地址
    private static final String CF_VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    // 工具类禁止实例化
    private CloudflareTurnstileUtil() {}

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
