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
package com.yuigneel.center.admin.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuigneel.center.admin.user.model.domain.AccountAvatar;
import org.apache.ibatis.annotations.Param;


/**
 * 账号头像 Mapper 接口
 *
 * @author yuigneel
 * @since 2026-05-06
 */
public interface AccountAvatarMapper extends BaseMapper<AccountAvatar> {

    /**
     * 根据UID和身份类型物理删除（忽略逻辑删除）
     *
     * @param uid          用户/管理员唯一业务UID
     * @param identityType 身份类型
     * @return 影响行数
     * @author yuigneel
     * @since 2026-05-06
     */
    int deleteByUidAndIdentityTypeIgnoreLogic(@Param("uid") Long uid, @Param("identityType") Integer identityType);
}
