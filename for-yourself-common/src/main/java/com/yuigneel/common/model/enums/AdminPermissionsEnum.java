package com.yuigneel.common.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AdminPermissionsEnum implements BaseEnum {
    ADMIN_LEVEL_ROOT(0, "超级管理员"),
    ADMIN_LEVEL_HIGH(1, "高级管理员"),
    ADMIN_LEVEL_MIDDLE(2, "中级管理员"),
    ADMIN_LEVEL_LOW(3, "低级管理员");
    @JsonValue
    @EnumValue
    private final Integer code;
    private final String name;

}
