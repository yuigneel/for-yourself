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

@Getter
@AllArgsConstructor
public enum AccountBanLevel implements BaseEnum {
    /**
     * S 级封禁 - 最严重违规，封禁 8760小时
     */
    LEVEL_S(8760, "S"),

    /**
     * A 级封禁 - 严重违规，封禁 2160 小时
     */
    LEVEL_A(2160, "A"),

    /**
     * B 级封禁 - 中等违规，封禁 720 小时
     */
    LEVEL_B(720, "B"),

    /**
     * C 级封禁 - 轻度违规，封禁 360 小时
     */
    LEVEL_C(360, "C"),

    /**
     * D 级封禁 - 轻微违规，封禁 72 小时
     */
    LEVEL_D(72, "D"),

    /**
     * E 级封禁 - 轻违规，封禁 24 小时
     */
    LEVEL_E(24, "E"),

    /**
     * F 级封禁 - 轻违规，封禁 9小时
     */
    LEVEL_F(9, "F"),

    /**
     * G 级封禁 - 最轻违规，封禁 6小时
     */
    LEVEL_G(6, "G"),

    /**
     * H 级封禁 - 极轻违规，封禁 3小时
     */
    LEVEL_H(3, "H");


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
     * 根据名称获取对应的封禁等级枚举
     *
     * @param name 枚举名称（如 "S 级封禁"）
     * @return 对应的枚举值，如果未找到则返回 null
     */
    public static AccountBanLevel getByName(String name) {
        for (AccountBanLevel level : values()) {
            if (level.getName().equals(name)) {
                return level;
            }
        }
        return null;
    }

    /**
     * 根据等级字母获取对应的封禁等级枚举
     *
     * @param letter 等级字母（如 "S", "A", "B"...）
     * @return 对应的枚举值，如果未找到则返回 null
     */
    public static AccountBanLevel getByLetter(String letter) {
        try {
            return valueOf("LEVEL_" + letter.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
