package com.yuigneel.center.admin.user.model.vo;

import com.yuigneel.center.user.api.model.enums.GenderEnum;
import com.yuigneel.common.model.enums.AccountStatusEnum;
import com.yuigneel.common.model.enums.AdminPermissionsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 管理员用户完整信息响应 VO
 * <p>包含管理员用户的所有字段信息，可根据实际需求删除不需要的字段</p>
 *
 * @author yulgnier
 * @date 2026-04-19
 */
@Data
@Schema(description = "管理员用户完整信息响应")
public class AdminUserInfoResponseVO {

    @Schema(description = "管理员唯一业务UID", example = "1234567890")
    private Long uid;
    
    @Schema(description = "登录邮箱(唯一)", example = "admin@example.com")
    private String email;
    
    @Schema(description = "管理员昵称(唯一)", example = "超级管理员")
    private String nickname;
    
    @Schema(description = "性别：0-未知 3-强男 2-男 1-弱男 -1-弱女 -2-女 -3-强女", example = "0")
    private GenderEnum gender;
    
    @Schema(description = "出生日期", example = "1990-01-01")
    private LocalDate birthday;

    @Schema(description = "平台入驻日期", example = "2024-01-01")
    private LocalDate joinDate;
    
    @Schema(description = "账户权限", example = "ADMIN_LEVEL_ROOT")
    private AdminPermissionsEnum accountPermission;
    
    @Schema(description = "账户状态", example = "ACCOUNT_STATUS_NORMAL")
    private AccountStatusEnum accountStatus;


}
