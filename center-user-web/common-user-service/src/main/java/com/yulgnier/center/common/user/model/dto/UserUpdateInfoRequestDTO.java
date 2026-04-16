package com.yulgnier.center.common.user.model.dto;

import com.yulgnier.center.common.user.model.enums.GenderEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户修改普通信息请求 DTO
 *
 * @author yulgnier
 * @date 2026-04-16
 */
@Data
@Schema(description = "用户修改普通信息请求")
public class UserUpdateInfoRequestDTO {
    
    @NotBlank(message = "昵称不能为空")
    @Schema(description = "用户昵称", example = "小明", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nickname;
    
    @NotNull(message = "性别不能为空")
    @Schema(description = "性别：1-男 2-女 0-未知", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private GenderEnum genderEnum;
    
    @Schema(description = "生日（可选）", example = "2000-01-01", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate birthday;
}
