package com.yuigneel.center.user.api.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 封禁等级枚举 - 用于定义不同严重程度的封禁时长
 *
 * @author yulgnier
 * @date 2026-04-03
 */
@Getter
@AllArgsConstructor
public enum EmailBanLevelEnum implements BaseEnum {
    /**
     * S 级封禁 - 最严重违规，封禁 24 小时（1440 分钟）
     */
    LEVEL_S(1440, "S"),
    
    /**
     * A 级封禁 - 严重违规，封禁 9 小时（540 分钟）
     */
    LEVEL_A(540, "A"),
    
    /**
     * B 级封禁 - 中等违规，封禁 6 小时（360 分钟）
     */
    LEVEL_B(360, "B"),
    
    /**
     * C 级封禁 - 轻度违规，封禁 3 小时（180 分钟）
     */
    LEVEL_C(180, "C"),
    
    /**
     * D 级封禁 - 轻微违规，封禁 30 分钟
     */
    LEVEL_D(30, "D"),
    
    /**
     * E 级封禁 - 最轻违规，封禁 5 分钟
     */
    LEVEL_E(5, "E"),

    /**
     * F 级封禁 - 最轻违规，封禁 5 分钟
     */
    LEVEL_F(5, "F"),

    /**
     * G 级封禁 - 警告，封禁 1 分钟
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
     * 根据名称获取对应的封禁等级枚举
     *
     * @param name 枚举名称（如 "S 级封禁"）
     * @return 对应的枚举值，如果未找到则返回 null
     */
    public static EmailBanLevelEnum getByName(String name) {
        for (EmailBanLevelEnum level : values()) {
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
    public static EmailBanLevelEnum getByLetter(String letter) {
        try {
            return valueOf("LEVEL_" + letter.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
