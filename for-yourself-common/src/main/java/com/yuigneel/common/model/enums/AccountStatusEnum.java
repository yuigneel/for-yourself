package com.yuigneel.common.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
@Getter
@RequiredArgsConstructor
public enum AccountStatusEnum implements BaseEnum{
    ACCOUNT_STATUS_NORMAL(0, "正常"),
    ACCOUNT_STATUS_WARNING(1, "警告"),
    ACCOUNT_STATUS_LOCKED(2, "锁定"),
    ACCOUNT_STATUS_FORCE_LOGOUT(3, "强制注销");
    @JsonValue
    @EnumValue
    private final Integer code;
    private final String name;
}
