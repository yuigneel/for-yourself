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
package com.yuigneel.center.user.api.model.dto;

import com.yuigneel.common.model.enums.AccountIdentityTypeEnum;
import com.yuigneel.common.model.enums.AccountStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 账户状态修改请求 DTO（通用，适用于普通用户和管理员）
 *
 * @author yulgnier
 * @since 2026-05-02
 */
@Data
@Schema(description = "账户状态修改请求")
public class AccountStatusUpdateRequestDTO {
    
    @NotNull(message = "目标账户 UID 不能为空")
    @Schema(description = "目标账户 UID", example = "1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long uid;
    
    @NotNull(message = "账户身份类型不能为空")
    @Schema(description = "账户身份类型：0-管理员 1-普通用户", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private AccountIdentityTypeEnum identityType;
    
    @NotNull(message = "目标账户状态不能为空")
    @Schema(description = "目标账户状态：0-正常 1-警告 2-封禁 3-强制注销", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private AccountStatusEnum targetStatus;
    
    @Schema(description = "异常状态到期时间等级（仅当目标状态为异常时填写，正常状态,强制删除无需填写）", example = "A")
    private String banLevel;

    @Schema(description = "异常状态理由", example = "用户没给up主逆羽风辰一键三联")
    private String banReason;
}


