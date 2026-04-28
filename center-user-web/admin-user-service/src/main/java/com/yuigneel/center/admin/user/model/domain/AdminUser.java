package com.yuigneel.center.admin.user.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;

import com.yuigneel.center.user.api.model.enums.GenderEnum;
import com.yuigneel.common.model.enums.AccountStatusEnum;
import com.yuigneel.common.model.enums.AdminPermissionsEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理员用户基础信息表
 * @TableName t_admin_user
 */
@Data
@TableName(value ="t_admin_user")
@EqualsAndHashCode(callSuper = true)
public class AdminUser extends BaseDomain {


    /**
     * 管理员唯一业务UID
     */
    @TableField(value = "uid")
    private Long uid;

    /**
     * 登录邮箱(唯一)
     */
    @TableField(value = "email")
    private String email;

    /**
     * 管理员昵称(唯一)
     */
    @TableField(value = "nickname")
    private String nickname;

    /**
     * BCrypt加密后的密码
     */
    @TableField(value = "password")
    private String password;

    /**
     * 性别：0-未知 3-强男 2-男 1-弱男 -1-弱女 -2-女 -3-强女
     */
    @TableField(value = "gender")
    private GenderEnum gender;

    /**
     * 出生日期
     */
    @TableField(value = "birthday")
    private LocalDate birthday;

    /**
     * 平台入驻日期
     */
    @TableField(value = "join_date")
    private LocalDate joinDate;

    /**
     * 账户权限：0-最高权限 数值越大权限越低 当前最低为3
     */
    @TableField(value = "account_permission")
    private AdminPermissionsEnum accountPermission;

    /**
     * 账户状态：3- 强制删除 2-禁用 1-警告 0-正常(默认0)
     */
    @TableField(value = "account_status")
    private AccountStatusEnum accountStatus;

    /**
     * 最后更新人 ID
     */
    @TableField(value = "update_by")
    private Long updateBy;
}