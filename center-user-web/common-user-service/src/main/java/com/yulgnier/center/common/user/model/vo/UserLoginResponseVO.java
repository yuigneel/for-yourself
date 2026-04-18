package com.yulgnier.center.common.user.model.vo;

import com.yulgnier.common.model.result.ResultCodeEnum;
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
    
    @Schema(description = "登录状态码", example = "A0800")
    private ResultCodeEnum resultCode;

    @Schema(description = "登录状态信息", example = "用户正常登录")
    private String resultMessage;
    
    /**
     * 设置 resultCode 时自动设置 resultMessage
     */
    public void setResultCode(ResultCodeEnum resultCode) {
        this.resultCode = resultCode;
        if (resultCode != null) {
            this.resultMessage = resultCode.getMessage();
        }
    }
}
