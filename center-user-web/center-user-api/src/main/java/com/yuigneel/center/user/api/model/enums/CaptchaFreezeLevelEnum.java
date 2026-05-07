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
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 验证码冻结等级枚举 - 用于定义验证码错误次数过多时邮箱的冻结时长
 * <p>当用户连续输入验证码错误达到上限后，系统会根据当前冻结等级自动升级并冻结邮箱发送功能</p>
 *
 * @author yulgnier
 * @date 2026-04-03
 */
@Getter
@AllArgsConstructor
public enum CaptchaFreezeLevelEnum implements BaseEnum {
    /**
     * S 级冻结 - 最严重，冻结 24 小时（1440 分钟）
     */
    LEVEL_S(1440, "S"),
    
    /**
     * A 级冻结 - 严重，冻结 9 小时（540 分钟）
     */
    LEVEL_A(540, "A"),
    
    /**
     * B 级冻结 - 中等，冻结 6 小时（360 分钟）
     */
    LEVEL_B(360, "B"),
    
    /**
     * C 级冻结 - 轻度，冻结 3 小时（180 分钟）
     */
    LEVEL_C(180, "C"),
    
    /**
     * D 级冻结 - 轻微，冻结 30 分钟
     */
    LEVEL_D(30, "D"),
    
    /**
     * E 级冻结 - 最轻，冻结 5 分钟
     */
    LEVEL_E(5, "E"),

    /**
     * F 级冻结 - 最轻，冻结 5 分钟
     */
    LEVEL_F(5, "F"),

    /**
     * G 级冻结 - 警告，冻结 1 分钟
     */
    LEVEL_G(1, "G");


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

    /**
     * 根据名称获取对应的冻结等级枚举
     *
     * @param name 枚举名称（如 "S", "A", "B"...）
     * @return 对应的枚举值，如果未找到则返回 null
     */
    public static CaptchaFreezeLevelEnum getByName(String name) {
        for (CaptchaFreezeLevelEnum level : values()) {
            if (level.getName().equals(name)) {
                return level;
            }
        }
        return null;
    }

    /**
     * 根据等级字母获取对应的冻结等级枚举
     *
     * @param letter 等级字母（如 "S", "A", "B"...）
     * @return 对应的枚举值，如果未找到则返回 LEVEL_G
     */
    public static CaptchaFreezeLevelEnum getByLetter(String letter) {
        try {
            return valueOf("LEVEL_" + letter.toUpperCase());
        } catch (IllegalArgumentException e) {
            return LEVEL_G;
        }
    }
}
