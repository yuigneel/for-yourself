package com.yulgnier.common.utils;

/**
 * 用户上下文工具类
 * 基于ThreadLocal存储当前请求的用户ID，实现线程隔离的用户信息传递
 */
public class UserContextUtil {
    private UserContextUtil() {
    }

    /**
     * 线程本地变量，存储当前用户的ID
     */
    private static final ThreadLocal<Long> uid = new ThreadLocal<>();

    /**
     * 线程本地变量，存储当前用户的身份类型
     */
    private static final ThreadLocal<String> identity = new ThreadLocal<>();

    /**
     * 线程本地变量，存储当前用户的管理员权限等级
     */
    private static final ThreadLocal<Integer> adminLevel = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     *
     * @param uid 用户ID
     */
    public static void setUid(Long uid) {
        UserContextUtil.uid.set(uid);
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID，未设置时返回null
     */
    public static Long getUid() {
        return uid.get();
    }

    /**
     * 设置当前用户身份
     *
     * @param identity 用户身份类型
     */
    public static void setIdentity(String identity) {
        UserContextUtil.identity.set(identity);
    }

    /**
     * 获取当前用户身份
     *
     * @return 用户身份类型，未设置时返回null
     */
    public static String getIdentity() {
        return identity.get();
    }

    /**
     * 设置当前用户的管理员权限等级
     *
     * @param adminLevel 管理员权限等级
     */
    public static void setAdminLevel(Integer adminLevel) {
        UserContextUtil.adminLevel.set(adminLevel);
    }

    /**
     * 获取当前用户的管理员权限等级
     *
     * @return 管理员权限等级，未设置时返回null
     */
    public static Integer getAdminLevel() {
        return adminLevel.get();
    }

    /**
     * 清除当前线程的用户信息，防止内存泄漏
     */
    public static void clear() {
        uid.remove();
        identity.remove();
        adminLevel.remove();
    }
}
