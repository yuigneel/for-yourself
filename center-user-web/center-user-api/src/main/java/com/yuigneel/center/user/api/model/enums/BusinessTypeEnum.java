package com.yuigneel.center.user.api.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

/**
 * 业务类型枚举 - 用于邮箱验证码的业务场景
 *
 * @author yulgnier
 * @date 2026-04-01
 */
@AllArgsConstructor
public enum BusinessTypeEnum implements BaseEnum {
    /**
     * 发送验证码业务
     */
    SEND_EMAIL(0,"验证码发送"),

    /**
     * 注册业务
     */
    REGISTER(1, "注册"),

    /**
     * 找回密码业务
     */
    FORGET_PASSWORD(2, "找回密码"),

    /**
     * 绑定邮箱业务
     */
    BIND_EMAIL(3, "绑定邮箱"),

    /**
     * 用户注销业务
     */
    CANCEL_USER(4, "用户注销");


    /**
     * MyBatis Plus: 数据库存储/读取时使用 code 值
     * Jackson: JSON 序列化时仅输出 code 值
     */
    @EnumValue
    @JsonValue
    private final Integer code;

    private final String name;

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
