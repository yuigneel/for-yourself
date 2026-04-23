package com.yulgnier.center.admin.user.model.dto;

import com.yulgnier.common.model.enums.AdminPermissionsEnum;
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
    
    @NotNull(message = "管理员UID不能为空")
    @Schema(description = "被修改的管理员UID", example = "1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long uid;
    
    @NotNull(message = "新的权限等级不能为空")
    @Schema(description = "新的账户权限等级", example = "ADMIN_LEVEL_HIGH", requiredMode = Schema.RequiredMode.REQUIRED)
    private AdminPermissionsEnum newPermission;
}
