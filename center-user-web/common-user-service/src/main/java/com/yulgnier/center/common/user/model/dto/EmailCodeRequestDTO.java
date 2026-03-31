package com.yulgnier.center.common.user.model.dto;

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
    
    @Schema(description = "业务类型：register-注册、forget-找回密码、bind-绑定邮箱", example = "register", requiredMode = Schema.RequiredMode.REQUIRED)
    private String businessType;
}
