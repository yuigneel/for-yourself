package com.yulgnier.common.utils;

public class UserContextUtil {
    private UserContextUtil() {
    }

    private static final ThreadLocal<Long> uid = new ThreadLocal<>();

    public static void setUid(Long uid) {
        UserContextUtil.uid.set(uid);
    }

    public static Long getUid() {
        return uid.get();
    }

    public static void clear() {
        uid.remove();
    }
}
