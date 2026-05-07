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
package com.yuigneel.center.common.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuigneel.center.common.user.mapper.AccountAvatarMapper;
import com.yuigneel.center.common.user.model.domain.AccountAvatar;
import com.yuigneel.center.common.user.service.CommonUserFileService;
import com.yuigneel.common.exception.ForYourselfException;
import com.yuigneel.common.model.result.ResultCodeEnum;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 普通用户文件服务实现类（基于云存储）
 * <p>
 * 实现CommonUserFileService接口，预留云存储方案的文件管理功能
 * </p>
 * <p>
 * 特殊事项：
 * - 当前为TODO状态，所有方法均抛出未实现异常
 * - 用于未来扩展云存储方案（如阿里云OSS、腾讯云COS等）
 * </p>
 *
 * @author Yu·Igneel
 * @since 2026-05-06
 */
@Service
public class CommonUserFileServiceByCloudImpl extends ServiceImpl<AccountAvatarMapper, AccountAvatar> implements CommonUserFileService {

    /**
     * 上传普通用户头像（待实现）
     *
     * @param file 头像文件
     * @author Yu·Igneel
     * @since 2026-05-06
     */
    @Override
    public void uploadAvatar(MultipartFile file) {
        //TODO
        throw new ForYourselfException(ResultCodeEnum.TODO, null);
    }

    /**
     * 获取普通用户头像URL（待实现）
     *
     * @return 头像URL
     * @author Yu·Igneel
     * @since 2026-05-06
     */
    @Override
    public String getAvatar() {
        //TODO
        throw new ForYourselfException(ResultCodeEnum.TODO, null);
    }

    /**
     * 物理删除普通用户头像（待实现）
     *
     * @param uid 普通用户ID
     * @author Yu·Igneel
     * @since 2026-05-06
     */
    @Override
    public void physicalDeleteAvatarByUid(Long uid) {
        //TODO
        throw new ForYourselfException(ResultCodeEnum.TODO, null);
    }

    /**
     * 物理删除普通用户头像（允许为空，待实现）
     *
     * @param uid 普通用户ID
     * @return 删除记录数
     * @author Yu·Igneel
     * @since 2026-05-06
     */
    @Override
    public int physicalDeleteAvatarByUidAllowNull(Long uid) {
        //TODO
        throw new ForYourselfException(ResultCodeEnum.TODO, null);
    }
}
