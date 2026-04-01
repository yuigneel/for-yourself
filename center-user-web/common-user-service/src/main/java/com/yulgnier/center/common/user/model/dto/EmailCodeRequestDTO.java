package com.yulgnier.center.common.user.model.dto;

import com.yulgnier.center.common.user.model.enums.BusinessTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 获取邮箱验证码请求 DTO
 *
 * @author yulgnier
 * @date 2026-03-31
 */
@Data
@Schema(description = "获取邮箱验证码请求")
public class EmailCodeRequestDTO {
    
    @Schema(description = "用户邮箱", example = "example@163.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
    
    @Schema(description = "业务类型：1-注册、2-找回密码、3-绑定邮箱", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private BusinessTypeEnum businessType;
    
    @Schema(description = "Cloudflare Turnstile 人机验证响应 token", example = "0x4AAAAAA...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cfTurnstileResponse;
}
