package com.yulgnier.center.admin.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员用户注销请求 DTO
 *
 * @author yulgnier
 * @date 2026-04-22
 */
@Data
@Schema(description = "管理员用户注销请求")
public class AdminUserLogoutRequestDTO {
    
    @NotBlank(message = "用户昵称不能为空")
    @Schema(description = "用户昵称", example = "管理员", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nickname;
    
    @NotBlank(message = "邮箱不能为空")
    @Schema(description = "用户邮箱", example = "admin@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
    
    @NotBlank(message = "密码不能为空")
    @Schema(description = "登录密码", example = "Abc123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
    
    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码（6位）", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;
    
    @NotBlank(message = "Cloudflare Turnstile 人机验证响应 token 不能为空")
    @Schema(description = "Cloudflare Turnstile 人机验证响应 token", example = "0x4AAAAAA...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cfTurnstileResponse;
}
