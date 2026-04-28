package com.yuigneel.center.admin.user.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yuigneel.common.model.enums.AccountIdentityTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账号头像表（统一管理用户和管理员头像）
 *
 * @author yulgnier
 * @date 2025
 */
@Data
@EqualsAndHashCode(callSuper = true) // 让 equals 和 hashCode 方法，同时对比【子类 + 父类】所有字段
@TableName(value = "t_account_avatar")
public class AccountAvatar extends BaseDomain {

    /**
     * 身份类型：0-管理员 1-普通用户
     */
    @TableField(value = "identity_type")
    private AccountIdentityTypeEnum identityType;

    /**
     * 用户/管理员唯一业务UID
     */
    @TableField(value = "uid")
    private Long uid;

    /**
     * 头像文件唯一标识符（MinIO 文件名）
     * <p>
     * 存储由 MinioUtil.uploadFile() 返回的唯一文件名，格式：UUID-原始文件名
     * 通过 MinioUtil.getPresignedUrl() 方法可生成临时访问URL
     */
    @TableField(value = "avatar_url")
    private String avatarUrl;
}
