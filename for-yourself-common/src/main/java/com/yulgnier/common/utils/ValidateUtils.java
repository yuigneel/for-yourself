package com.yulgnier.common.utils;

import java.util.regex.Pattern;

/**
 * 通用参数校验工具类
 */
public class ValidateUtils {

    // 1. 私有化构造方法：禁止外面 new 对象
    private ValidateUtils() {
    }

    // 邮箱正则
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$"
    );

    // 密码正则：8-20位，必须包含字母+数字
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-zA-Z])[a-zA-Z0-9]{8,20}$"
    );

    // 所有方法都加 static
    /**
     * 宽松版用户名校验
     * 仅校验：非空 + 长度2-20位，其余字符无任何限制
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        int length = username.trim().length();
        return length >= 2 && length <= 20;
    }

    public static boolean isValidEmail(String email) {
        return email != null && !email.isBlank()
                && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && !password.isBlank()
                && PASSWORD_PATTERN.matcher(password).matches();
    }
}