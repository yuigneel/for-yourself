package com.yulgnier.center.admin.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户换绑邮箱请求 DTO
 *
 * @author yulgnier
 * @date 2026-04-18
 */
@Data
@Schema(description = "用户换绑邮箱请求")
public class UserChangeEmailRequestDTO {
    
    @NotBlank(message = "旧邮箱不能为空")
    @Schema(description = "旧邮箱", example = "old@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String oldEmail;
    
    @NotBlank(message = "新邮箱不能为空")
    @Schema(description = "新邮箱", example = "new@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newEmail;
    
    @NotBlank(message = "密码不能为空")
    @Schema(description = "用户密码", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
