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
package com.yuigneel.center.user.api.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 登录方式枚举
 */
public enum LoginTypeEnum implements BaseEnum {
    
    /**
     * 用户名登录
     */
    USERNAME(1, "用户名"),
    
    /**
     * 邮箱登录
     */
    EMAIL(2, "邮箱"),
    
    /**
     * 手机号登录
     */
    PHONE(3, "手机号"),
    
    /**
     * UID 登录
     */
    UID(4, "UID");
    
    /**
     * 核心注解作用说明：
     * 1. @EnumValue (MyBatis Plus)：
     *    - 存库：将枚举的 code 值存入数据库字段
     *    - 查库：将数据库数字值映射为对应枚举实例
     * 2. @JsonValue (Jackson)：
     *    - 序列化：返回前端时仅输出 code 值，不返回枚举名/完整对象
     */
    @EnumValue
    @JsonValue
    private final Integer code;
    
    private final String name;
    
    LoginTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    
    @Override
    public Integer getCode() {
        return this.code;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
}
