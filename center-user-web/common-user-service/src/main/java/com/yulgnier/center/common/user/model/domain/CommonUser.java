package com.yulgnier.center.common.user.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;

import com.yulgnier.center.common.user.model.enums.GenderEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 普通用户基础信息表
 * @ TableName t_common_user
 */
@Data
@EqualsAndHashCode(callSuper = true) // 让 equals 和 hashCode 方法，同时对比【子类 + 父类】所有字段
@TableName(value ="t_common_user")
public class CommonUser extends BaseDomain{

    /**
     * 用户唯一业务UID
     */
    private Long uid;

    /**
     * 登录邮箱(唯一)
     */
    private String email;

    /**
     * 用户昵称(唯一)
     */
    private String nickname;

    /**
     * BCrypt加密后的密码
     */
    private String password;

    /**
     * 性别：0-未知 3-强男 2-男 1-弱男 -1-弱女 -2-女 -3-强女
     */
    private GenderEnum gender;

    /**
     * 出生日期
     */
    private LocalDate birthday;

    /**
     * 平台入驻日期
     */
    private LocalDate joinDate;

    /**
     * 账户状态：2-禁用 1-警告 0-正常(默认0)
     */
    private Integer accountStatus;
}