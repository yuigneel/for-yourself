package com.yulgnier.center.admin.user.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GenderEnum implements BaseEnum {
    /**
     * 枚举常量定义
     * 性别分级：基于激素水平相对于平均值的程度 伊格尼尔：为了对付不讲理的100多种性别认同，这里要么你好好的按染色体x y 来分男女
     * 如果你要说什么公平，那我觉得按这个反而很公平（染色体加激素等水平）
     */
    STRONG_MALE(3, "强男"),      // 男性激素水平严重高于平均水平
    MALE(2, "男"),               // 正常男性
    WEAK_MALE(1, "弱男"),        // 男性激素严重低于平均水平
    UNKNOWN(0, "未知"),          // 未知性别
    WEAK_FEMALE(-1, "弱女"),     // 女性激素严重低于平均水平
    FEMALE(-2, "女"),            // 正常女性
    STRONG_FEMALE(-3, "强女");   // 女性激素水平严重高于平均水平

    /**
     * 核心注解作用说明：
     * 1. @EnumValue (MyBatis Plus)：
     *    - 存库：将枚举的 code 值（3/2/1/0/-1/-2/-3）存入数据库字段
     *    - 查库：将数据库数字值映射为对应枚举实例
     * 2. @JsonValue (Jackson)：
     *    - 序列化：返回前端时仅输出 code 值，不返回枚举名/完整对象
     */
    @EnumValue
    @JsonValue
    private final Integer code;

    private final String name;

    GenderEnum(Integer code, String name) {
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
