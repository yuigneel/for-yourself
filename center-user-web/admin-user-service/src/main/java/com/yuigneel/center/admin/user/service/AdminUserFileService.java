package com.yuigneel.center.admin.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuigneel.center.admin.user.model.domain.AccountAvatar;
import org.springframework.web.multipart.MultipartFile;

/**
 * 管理员用户文件服务接口
 * <p>
 * 提供管理员用户头像上传、获取、删除等文件管理功能
 * </p>
 *
 * @author yuigneel
 * @since 2026-05-06
 */
public interface AdminUserFileService extends IService<AccountAvatar> {

    /**
     * 上传管理员头像
     *
     * @param file 头像文件，支持常见图片格式
     * @author yuigneel
     * @since 2026-05-06
     */
    void uploadAvatar(MultipartFile file);

    /**
     * 获取当前管理员的头像URL
     *
     * @return 头像临时访问URL，如果不存在则返回null
     * @author yuigneel
     * @since 2026-05-06
     */
    String getAvatar();

    /**
     * 物理删除指定UID的管理员头像
     *
     * @param uid 管理员用户ID
     * @author yuigneel
     * @since 2026-05-06
     */
    void physicalDeleteAvatarByUid(Long uid);

    /**
     * 物理删除指定UID的管理员头像（允许为空）
     *
     * @param uid 管理员用户ID
     * @return 删除的记录数，0表示未找到记录
     * @author yuigneel
     * @since 2026-05-06
     */
    int physicalDeleteAvatarByUidAllowNull(Long uid);
}
