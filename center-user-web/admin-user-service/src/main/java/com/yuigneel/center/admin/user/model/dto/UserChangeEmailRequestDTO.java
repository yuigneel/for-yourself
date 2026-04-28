package com.yuigneel.center.admin.user.model.dto;

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

    @NotBlank(message = "新邮箱不能为空")
    @Schema(description = "新邮箱地址", example = "newemail@163.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newEmail;

    @NotBlank(message = "用户密码不能为空")
    @Schema(description = "用户当前密码", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "新邮箱验证码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String verificationCode;
}
