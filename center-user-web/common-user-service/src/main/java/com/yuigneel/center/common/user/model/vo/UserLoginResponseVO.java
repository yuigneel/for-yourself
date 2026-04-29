package com.yuigneel.center.common.user.model.vo;

import com.yuigneel.center.user.api.model.enums.LoginStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户登录响应 VO
 *
 * @author yulgnier
 * @date 2026-04-18
 */
@Data
@Schema(description = "用户登录响应")
public class UserLoginResponseVO {
    
    @Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "登录状态枚举", example = "USER_NORMAL_LOGIN")
    private LoginStatusEnum resultCodeEnum;
}
