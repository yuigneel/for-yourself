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
     * JWT 中声明用户身份的key和value
     */
    public static final String IDENTITY_KEY = "identity";
    public static final String IDENTITY_COMMON_USER_VALUE= "common_user";
    public static final String IDENTITY_ADMIN_USER_VALUE= "admin_user";

    /**
     * JWT 中封装的admin权限等级的key和value
     */
    public static final String ADMIN_LEVEL_KEY = "admin_level";
    public static final Integer ADMIN_LEVEL_ROOT_VALUE = 0;     //（根账户 / 超级管理员）
    public static final Integer ADMIN_LEVEL_HIGH_VALUE = 1;     //（高级管理员）
    public static final Integer ADMIN_LEVEL_MIDDLE_VALUE = 2;   //（中级管理员）
    public static final Integer ADMIN_LEVEL_LOW_VALUE = 3;      //（低级管理员）

    /**
     * 项目中字段的分隔符
     * key                             value
     * 业务类型（name）+分隔符+邮箱            验证码+分隔符+尝试次数
     * 业务类型（验证码发送）+分隔符+邮箱         封禁等级+分隔符+封禁时间
     */
    public static final String SEPARATOR = ":";
}
