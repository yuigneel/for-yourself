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
package com.yuigneel.center.user.api.model.vo;

import com.yuigneel.center.user.api.model.enums.GenderEnum;
import com.yuigneel.common.model.enums.AccountStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户基础信息响应 VO
 *
 * @author yulgnier
 * @date 2026-04-17
 */
@Data
@Schema(description = "用户基础信息响应")
public class CommonUserInfoResponseVO {

    @Schema(description = "用户唯一业务 UID", example = "1234567890")
    private Long uid;

    @Schema(description = "用户邮箱", example = "example@163.com")
    private String email;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "用户昵称", example = "yulgnier")
    private String nickname;

    @Schema(description = "性别", example = "MALE")
    private GenderEnum gender;

    @Schema(description = "出生日期", example = "2000-01-01")
    private LocalDate birthday;

    @Schema(description = "平台入驻日期", example = "2024-01-01")
    private LocalDate joinDate;

    @Schema(description = "账户状态", example = "ACCOUNT_STATUS_NORMAL")
    private AccountStatusEnum accountStatus;

    @Schema(description = "逻辑删除删除", example = "1")
    private Integer isDeleted;
}
