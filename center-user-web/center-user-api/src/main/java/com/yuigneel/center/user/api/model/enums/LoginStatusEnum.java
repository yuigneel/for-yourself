package com.yuigneel.center.user.api.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录状态枚举 - 用于标识用户的登录状态类型
 *
 * @author yulgnier
 * @date 2026-04-29
 */
@Getter
@AllArgsConstructor
public enum LoginStatusEnum implements BaseEnum {
    
    /**
     * 用户正常登录
     */
    NORMAL_LOGIN(1, "正常登录"),
    
    /**
     * 用户取消注销并登录
     */
    CANCEL_UNREGISTER_LOGIN(2, "取消注销并登录");

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