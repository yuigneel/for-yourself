package com.yulgnier.center.admin.user.model.dto;

import com.yulgnier.center.user.api.model.enums.LoginTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户登录请求 DTO
 *
 * @author yulgnier
 * @date 2026-04-18
 */
@Data
@Schema(description = "用户登录请求")
public class UserLoginRequestDTO {
    
    @NotNull(message = "登录方式不能为空")
    @Schema(description = "登录方式枚举", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private LoginTypeEnum loginType;
    
    @NotBlank(message = "用户名/邮箱/手机号/UID 不能为空")
    @Schema(description = "登录账号（根据登录类型传入对应值）", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    
    @NotBlank(message = "密码不能为空")
    @Schema(description = "登录密码", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
    
    @NotBlank(message = "Cloudflare Turnstile 人机验证响应 token 不能为空")
    @Schema(description = "Cloudflare Turnstile 人机验证响应 token", example = "0x4AAAAAA...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cfTurnstileResponse;
}
