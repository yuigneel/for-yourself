package com.yulgnier.center.common.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户修改密码请求 DTO
 *
 * @author yulgnier
 * @date 2026-04-18
 */
@Data
@Schema(description = "用户修改密码请求")
public class UserUpdatePasswordRequestDTO {
    
    @NotBlank(message = "原始密码不能为空")
    @Schema(description = "原始密码", example = "oldPassword123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String oldPassword;
    
    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码", example = "newPassword456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;
}
