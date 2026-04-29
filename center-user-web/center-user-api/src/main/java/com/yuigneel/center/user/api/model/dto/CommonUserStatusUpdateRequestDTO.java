package com.yuigneel.center.user.api.model.dto;

import com.yuigneel.common.model.enums.AccountStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 普通用户账号状态修改请求 DTO
 *
 * @author yulgnier
 * @since 2026-04-23
 */
@Data
@Schema(description = "普通用户账号状态修改请求")
public class CommonUserStatusUpdateRequestDTO {
    
    @NotNull(message = "普通用户 UID不能为空")
    @Schema(description = "被修改的普通用户 UID", example = "1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long uid;
    
    @NotNull(message = "新的账户状态不能为空")
    @Schema(description = "新的账户状态", example = "ACCOUNT_STATUS_NORMAL", requiredMode = Schema.RequiredMode.REQUIRED)
    private AccountStatusEnum newStatus;
}
