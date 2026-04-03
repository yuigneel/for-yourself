package com.yulgnier.center.common.user.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.yulgnier.center.common.user.model.enums.GenderEnum;
import lombok.Data;

/**
 * 普通用户基础信息表
 * @TableName t_common_user
 */
@Data
@TableName(value ="t_common_user")
public class CommonUser {
    /**
     * 主键自增ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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

    /**
     * 逻辑删除：0-未删除 1-已删除（默认0）
     */
    private Integer isDeleted;

    /**
     * 创建时间(自动生成)
     */
    private LocalDateTime createTime;

    /**
     * 更新时间(自动更新)
     */
    private LocalDateTime updateTime;

    /**
     * 最后更新人ID（默认null）
     */
    private Long updateBy;
}