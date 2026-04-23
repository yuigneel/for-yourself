package com.yulgnier.common.model.constants;

public final class AuthConstants {
    private AuthConstants() {
        // 防止反射破坏（可选，加了更严谨）
        throw new AssertionError("不允许实例化常量类");
    }

    /**
     * JWT 中封装的用户id的key
     */
    public static final String UID_KEY = "uid";

    /**
     * link 请求头
     */
    public static final String LINK_KEY = "link";

    /**
     * 身份常量
     */
    public static final String AUTH_TYPE_ADMIN = "admin";
    public static final String AUTH_TYPE_COMMON = "common";

    /**
     * 项目中字段的分隔符
     * key                             value
     * 业务类型（name）+分隔符+邮箱            验证码+分隔符+尝试次数
     * 业务类型（验证码发送）+分隔符+邮箱         封禁等级+分隔符+封禁时间
     */
    public static final String SEPARATOR = ":";
}
