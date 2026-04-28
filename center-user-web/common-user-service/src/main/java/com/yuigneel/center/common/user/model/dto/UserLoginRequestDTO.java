package com.yuigneel.center.common.user.model.dto;

import com.yuigneel.center.user.api.model.enums.LoginTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户登录请求 DTO
 */
@Data
@Schema(description = "用户登录请求")
public class UserLoginRequestDTO {
    
    @NotBlank(message = "用户名不能为空")  // 用户名不能为空，会触发该校验不仅仅只是提示
    @Schema(description = "登录账号（用户名/邮箱/手机号/UID）", example = "小明", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    
    @NotBlank(message = "密码不能为空")
    @Schema(description = "登录密码", example = "Abc123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String pw;
    
    @NotNull(message = "登录方式不能为空")
    @Schema(description = "登录方式：1-用户名 2-邮箱 3-手机号 4-UID", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private LoginTypeEnum loginType;

    @NotBlank(message = "Cloudflare Turnstile 人机验证响应 token 不能为空")
    @Schema(description = "Cloudflare Turnstile 人机验证响应 token", example = "0x4AAAAAA...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cfTurnstileResponse;
}
