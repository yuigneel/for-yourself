package com.yuigneel.center.common.user.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yuigneel.center.user.api.model.enums.GenderEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

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
    @Schema(description = "用户昵称", example = "小明", requiredMode = Schema.RequiredMode.REQUIRED, type = "string")
    private String nickname;

    @Schema(description = "性别：2-男 -2-女 0-未知 (可选)", example = "0", requiredMode = Schema.RequiredMode.NOT_REQUIRED, type = "integer")
    private GenderEnum genderEnum;
    
    @Schema(description = "生日（可选）", example = "2000-01-01", requiredMode = Schema.RequiredMode.NOT_REQUIRED, type = "string", format = "date")

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
}
