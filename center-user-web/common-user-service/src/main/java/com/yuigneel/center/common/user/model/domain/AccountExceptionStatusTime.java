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
package com.yuigneel.center.common.user.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yuigneel.common.model.enums.AccountIdentityTypeEnum;
import com.yuigneel.common.model.enums.AccountStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账号异常状态时间表
 * @TableName t_account_exception_status_time
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_account_exception_status_time")
public class AccountExceptionStatusTime extends BaseDomain {

    /**
     * 账号UID（雪花ID）
     */
    @TableField(value = "uid")
    private Long uid;

    /**
     * 身份类型：0-管理员 1-普通用户（对应AccountIdentityTypeEnum枚举）
     */
    @TableField(value = "identity_type")
    private AccountIdentityTypeEnum identityType;

    /**
     * 状态类型：1-警告 2-封禁
     */
    @TableField(value = "exception_type")
    private AccountStatusEnum exceptionType;

    /**
     * 异常状态到期时间（精确到秒）
     */
    @TableField(value = "expire_time")
    private java.time.LocalDateTime expireTime;

    /**
     * 异常原因/封禁理由
     */
    @TableField(value = "reason")
    private String reason;

    /**
     * 更新人UID（操作的管理员UID，NULL表示系统自动更新）
     */
    @TableField(value = "update_by")
    private Long updateBy;
}
