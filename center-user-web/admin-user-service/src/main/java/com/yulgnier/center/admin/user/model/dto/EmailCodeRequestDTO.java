package com.yulgnier.center.admin.user.model.dto;

import com.yulgnier.center.admin.user.model.enums.BusinessTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 获取邮箱验证码请求 DTO
 *
 * @author yulgnier
 * @date 2026-04-18
 */
@Data
@Schema(description = "获取邮箱验证码请求")
public class EmailCodeRequestDTO {
    
    @NotBlank(message = "邮箱不能为空")
    @Schema(description = "用户邮箱", example = "example@163.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotNull(message = "业务不能为空")
    @Schema(description = "业务类型：1-注册、2-找回密码、3-绑定邮箱", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private BusinessTypeEnum businessType;

    @NotBlank(message = "Cloudflare Turnstile 人机验证响应 token 不能为空")
    @Schema(description = "Cloudflare Turnstile 人机验证响应 token", example = "0x4AAAAAA...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cfTurnstileResponse;
}
