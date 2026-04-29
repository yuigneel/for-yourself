package com.yuigneel.center.admin.user.model.vo;

import com.yuigneel.center.user.api.model.enums.LoginStatusEnum;
import com.yuigneel.common.model.result.ResultCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 管理员登录响应 VO
 *
 * @author yulgnier
 * @date 2026-04-19
 */
@Data
@Schema(description = "管理员登录响应")
public class AdminUserLoginResponseVO {
    
    @Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "登录状态枚举", example = "USER_NORMAL_LOGIN")
    private LoginStatusEnum resultCodeENum;
}
