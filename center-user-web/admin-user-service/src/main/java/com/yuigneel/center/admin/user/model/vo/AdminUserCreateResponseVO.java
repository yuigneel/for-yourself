package com.yuigneel.center.admin.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员创建响应 VO
 *
 * @author yulgnier
 * @date 2026-04-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员创建响应")
public class AdminUserCreateResponseVO {
    
    @Schema(description = "账户昵称", example = "新管理员")
    private String nickname;
    
    @Schema(description = "原始密码（明文，请妥善保存）", example = "Abc123456")
    private String originalPassword;
}
