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


import com.fasterxml.jackson.annotation.JsonProperty;
import com.yuigneel.center.user.api.model.enums.GenderEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 用户注册请求 DTO
 */
@Data
@Schema(description = "用户注册请求")
public class UserRegisterRequestDTO {
    @JsonProperty("email") // 前端字段名为 email
    @NotBlank(message = "邮箱不能为空")
    @Schema(description = "用户邮箱", example = "example@email.com", requiredMode = Schema.RequiredMode.REQUIRED, type = "string")
    private String email;

    @NotBlank(message = "昵称不能为空")
    @Schema(description = "用户昵称,非空 + 长度 2-20 位，其余字符无任何限制", example = "小明", requiredMode = Schema.RequiredMode.REQUIRED, type = "string")
    private String nickname;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "登录密码：8-20 位，必须包含字母和数字和符号", example = "Abc123456.", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8, maxLength = 20, type = "string")
    private String password;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码6位固定", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 6, minLength = 6, type = "string")
    private String code;

    @DateTimeFormat(pattern = "yyyy-MM-dd") // 前端日期格式
    @Schema(description = "实际出生日期", example = "2000-01-01", requiredMode = Schema.RequiredMode.NOT_REQUIRED, type = "string", format = "date")
    private LocalDate birthday;

    @NotNull(message = "性别不能为空")
    @Schema(description = "性别：0-未知 3-强男 2-男 1-弱男 -1-弱女 -2-女 -3-强女", example = "0", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer")
    private GenderEnum genderEnum;

    @NotBlank(message = "Cloudflare Turnstile 人机验证响应 token 不能为空")
    @Schema(description = "Cloudflare Turnstile 人机验证响应 token", example = "0x4AAAAAA...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cfTurnstileResponse;
}
