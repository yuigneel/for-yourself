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
package com.yuigneel.center.admin.user.model.dto;

import com.yuigneel.common.model.enums.AdminPermissionsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员权限修改请求 DTO
 *
 * @author yulgnier
 * @date 2026-04-23
 */
@Data
@Schema(description = "管理员权限修改请求")
public class AdminUserPermissionUpdateRequestDTO {
    
    @NotNull(message = "管理员 UID 不能为空")
    @Schema(description = "被修改的管理员 UID", example = "1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long uid;
    
    @NotNull(message = "新的权限等级不能为空")
    @Schema(description = "新的账户权限等级", example = "ADMIN_LEVEL_HIGH", requiredMode = Schema.RequiredMode.REQUIRED)
    private AdminPermissionsEnum newPermission;
}
