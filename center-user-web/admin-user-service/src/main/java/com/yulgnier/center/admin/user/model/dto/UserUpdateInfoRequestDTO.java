package com.yulgnier.center.admin.user.model.dto;

import com.yulgnier.center.user.api.model.enums.GenderEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户更新信息请求 DTO
 *
 * @author yulgnier
 * @date 2026-04-18
 */
@Data
@Schema(description = "用户更新信息请求")
public class UserUpdateInfoRequestDTO {
    
    @NotBlank(message = "昵称不能为空")
    @Schema(description = "用户昵称", example = "管理员", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nickname;
    
    @Schema(description = "性别枚举", example = "1")
    private GenderEnum gender;
    
    @Schema(description = "生日", example = "1990-01-01")
    private LocalDate birthday;
}
