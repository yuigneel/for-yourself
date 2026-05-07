/*
 * For Yourself - A graduation project by the author, serving as a demonstration for the future complete project ecosystem
 * Copyright (C) 2026  Yu·Igneel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.yuigneel.center.common.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuigneel.center.common.user.model.domain.AccountAvatar;
import org.springframework.web.multipart.MultipartFile;

/**
 * 普通用户文件服务接口
 * <p>
 * 提供普通用户头像上传、获取、删除等文件管理功能
 * </p>
 *
 * @author Yu·Igneel
 * @since 2026-05-06
 */
public interface CommonUserFileService extends IService<AccountAvatar> {

    /**
     * 上传普通用户头像
     *
     * @param file 头像文件，支持常见图片格式
     * @author Yu·Igneel
     * @since 2026-05-06
     */
    void uploadAvatar(MultipartFile file);

    /**
     * 获取当前普通用户的头像URL
     *
     * @return 头像临时访问URL，如果不存在则返回null
     * @author Yu·Igneel
     * @since 2026-05-06
     */
    String getAvatar();

    /**
     * 物理删除指定UID的普通用户头像
     *
     * @param uid 普通用户ID
     * @author Yu·Igneel
     * @since 2026-05-06
     */
    void physicalDeleteAvatarByUid(Long uid);

    /**
     * 物理删除指定UID的普通用户头像（允许为空）
     *
     * @param uid 普通用户ID
     * @return 删除的记录数，0表示未找到记录
     * @author Yu·Igneel
     * @since 2026-05-06
     */
    int physicalDeleteAvatarByUidAllowNull(Long uid);
}
