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
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yuigneel.center.user.api.model.enums.GenderEnum;
import com.yuigneel.common.model.enums.AccountStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 普通用户基础信息表
 * @ TableName t_common_user
 */
@Data
@EqualsAndHashCode(callSuper = true) // 让 equals 和 hashCode 方法，同时对比【子类 + 父类】所有字段
@TableName(value ="t_common_user")
public class CommonUser extends BaseDomain{

    /**
     * 用户唯一业务UID
     */
    @TableField(value = "uid")
    private Long uid;

    /**
     * 登录邮箱(唯一)
     */
    @TableField(value = "email")
    private String email;

    /**
     * 用户昵称(唯一)
     */
    @TableField(value = "nickname")
    private String nickname;

    /**
     * BCrypt加密后的密码
     */
    @JsonIgnore // 忽略 JSON化此字段
    @TableField(value = "password")
    private String password;

    /**
     * 性别：0-未知 3-强男 2-男 1-弱男 -1-弱女 -2-女 -3-强女（必填，默认0）
     */
    @TableField(value = "gender")
    private GenderEnum gender;

    /**
     * 出生日期
     */
    @TableField(value = "birthday")
    private LocalDate birthday;

    /**
     * 平台入驻日期
     */
    @TableField(value = "join_date")
    private LocalDate joinDate;

    /**
     * 账户状态：3- 强制删除 2-禁用 1-警告 0-正常(默认0)
     */
    @TableField(value = "account_status")
    private AccountStatusEnum accountStatus;

    /**
     * 最后更新人ID（默认null）
     */
    @TableField(value = "update_by")
    private Long updateBy;
}