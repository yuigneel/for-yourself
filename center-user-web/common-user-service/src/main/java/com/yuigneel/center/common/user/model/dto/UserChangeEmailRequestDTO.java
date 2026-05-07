/*
 * For Yourself - A graduation project by the author, serving as a demonstration for the future complete project ecosystem
 * Copyright (C) 2026  Yu·Igneel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.yuigneel.center.common.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户换绑邮箱请求 DTO
 *
 * @author yulgnier
 * @date 2026-04-17
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
