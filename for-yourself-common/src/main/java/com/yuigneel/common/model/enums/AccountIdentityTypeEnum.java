package com.yuigneel.common.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountIdentityTypeEnum implements BaseEnum{
    ADMIN(0, "管理员"),
    USER(1, "用户");
    @JsonValue
    @EnumValue
    private final Integer code;
    private final String name;
}
