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
package com.yuigneel.center.user.api.model.dto;

import com.yuigneel.center.user.api.model.enums.BusinessTypeEnum;
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