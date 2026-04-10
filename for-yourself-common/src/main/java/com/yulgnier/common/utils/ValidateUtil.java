package com.yulgnier.common.utils;

import java.util.regex.Pattern;

/**
 * 通用参数校验工具类
 */
public class ValidateUtil {

    // 1. 私有化构造方法：禁止外面 new 对象
    private ValidateUtil() {
    }

    // 邮箱正则
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$"
    );

    // 密码正则：8-32位，必须包含字母+数字+特殊符号（允许中间有空格）
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9]).{8,32}$"
    );

    // 禁止使用的敏感用户名列表（小写）
    private static final java.util.Set<String> FORBIDDEN_USERNAMES = java.util.Set.of(
            "admin", "administrator", "root", "superuser",
            "system", "sys", "operator", "manager",
            "null", "none", "undefined", "nil",
            "test", "guest", "anonymous", "default",
            "user", "username", "password", "login",
            "support", "help", "service", "info",
            "webmaster", "postmaster", "hostmaster", "abuse",
            "security", "noreply", "no-reply", "donotreply"
    );

    // 所有方法都加 static
    /**
     * 用户名校验（增强版）
     * 校验规则：
     * 1. 非空且长度 2-20 位
     * 2. 不能包含首尾空格（防止恶意填充空格绕过校验）
     * 3. 不能是 null、none、undefined 等无效值（不区分大小写）
     * 4. 不能是 admin、root、system 等系统保留用户名（不区分大小写）
     * 5. 支持任意字符（包括中文、特殊符号等）
     *
     * @param username 待校验的用户名
     * @return 校验通过返回 true，否则返回 false
     */
    public static boolean isValidUsername(String username) {
        // 1. 基础非空校验
        if (username == null || username.isBlank()) {
            return false;
        }
        
        // 2. 禁止首尾空格（防止 "   用户名   " 这种极端情况）
        if (!username.equals(username.trim())) {
            return false;
        }
        
        // 3. 校验长度（2-20 位）
        int length = username.length();
        if (length < 2 || length > 20) {
            return false;
        }
        
        // 4. 检查是否为敏感/保留用户名（不区分大小写）
        String lowerCase = username.toLowerCase();
        if (FORBIDDEN_USERNAMES.contains(lowerCase)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 邮箱格式校验
     * 使用正则表达式验证邮箱地址的有效性
     *
     * @param email 待校验的邮箱地址
     * @return 校验通过返回 true，否则返回 false
     */
    public static boolean isValidEmail(String email) {
        return email != null && !email.isBlank()
                && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * 密码强度校验
     * 要求：8-32 位，必须同时包含字母、数字和特殊符号（如 . ! @ # 等），不允许首尾空格
     *
     * @param password 待校验的密码
     * @return 校验通过返回 true，否则返回 false
     */
    public static boolean isValidPassword(String password) {
        // 1. 基础非空校验
        if (password == null || password.isBlank()) {
            return false;
        }
        
        // 2. 禁止首尾空格（防止恶意填充空格绕过校验）
        if (!password.equals(password.trim())) {
            return false;
        }
        
        // 3. 使用正则校验：必须包含字母+数字+特殊符号，长度8-32位（允许中间有空格）
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}