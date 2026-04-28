package com.yuigneel.center.common.user.model.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.yuigneel.center.user.api.model.enums.GenderEnum;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "用户邮箱", example = "example@email.com", required = true)
    private String email;

    @Schema(description = "用户昵称,非空 + 长度 2-20 位，其余字符无任何限制", example = "小明", required = true)
    private String nickname;

    @Schema(description = "登录密码：8-20 位，必须包含字母和数字", example = "Abc123456", required = true, minLength = 8, maxLength = 20)
    private String password;

    @Schema(description = "验证码6位固定", example = "123456", required = true, maxLength = 6, minLength = 6)
    private String code;

    @DateTimeFormat(pattern = "yyyy-MM-dd") // 前端日期格式
    @Schema(description = "实际出生日期", example = "2000-01-01", required = false)
    private LocalDate birthday;

    @Schema(description = "性别：0-未知 3-强男 2-男 1-弱男 -1-弱女 -2-女 -3-强女", example = "0", required = false)
    private GenderEnum genderEnum;

    @Schema(description = "Cloudflare Turnstile 人机验证响应 token", example = "0x4AAAAAA...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cfTurnstileResponse;
}
