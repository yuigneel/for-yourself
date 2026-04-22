package com.yulgnier.center.admin.user.model.dto;

import com.yulgnier.common.model.enums.AdminPermissionsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员创建请求 DTO
 *
 * @author yulgnier
 * @date 2026-04-22
 */
@Data
@Schema(description = "管理员创建请求")
public class AdminUserCreateRequestDTO {
    
    @NotBlank(message = "用户邮箱不能为空")
    @Schema(description = "用户邮箱", example = "newadmin@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
    
    @NotNull(message = "权限等级不能为空")
    @Schema(description = "账户权限等级", example = "ADMIN_LEVEL_HIGH", requiredMode = Schema.RequiredMode.REQUIRED)
    private AdminPermissionsEnum accountPermission;
}
