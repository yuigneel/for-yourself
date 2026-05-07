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
package com.yuigneel.common.model.constants;

public final class AuthConstants {
    private AuthConstants() {
        // 防止反射破坏（可选，加了更严谨）
        throw new AssertionError("不允许实例化常量类");
    }

    /**
     * JWT 中封装的用户id的key
     */
    public static final String UID_KEY = "uid";

    /**
     * link 请求头
     */
    public static final String LINK_KEY = "link";


    /**
     * 项目中字段的分隔符
     * key                             value
     * 业务类型（name）+分隔符+邮箱            验证码+分隔符+尝试次数
     * 业务类型（验证码发送）+分隔符+邮箱         封禁等级+分隔符+封禁时间
     */
    public static final String SEPARATOR = ":";
}
