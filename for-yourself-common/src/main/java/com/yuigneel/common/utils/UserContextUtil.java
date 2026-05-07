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
package com.yuigneel.common.utils;

/**
 * 用户上下文工具类
 * 基于ThreadLocal存储当前请求的用户ID，实现线程隔离的用户信息传递
 */
public class UserContextUtil {
    private UserContextUtil() {
    }

    /**
     * 线程本地变量，存储当前用户的ID
     */
    private static final ThreadLocal<Long> uid = new ThreadLocal<>();


    /**
     * 线程本地变量，存储link验证token
     */
    private static final ThreadLocal<String> link = new ThreadLocal<>();


    /**
     * 设置当前用户ID
     *
     * @param uid 用户ID
     */
    public static void setUid(Long uid) {
        UserContextUtil.uid.set(uid);
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID，未设置时返回null
     */
    public static Long getUid() {
        return uid.get();
    }

    /**
     * 设置link验证token
     *
     * @param token link验证token
     */
    public static void setLink(String token) {
        link.set(token);
    }

    /**
     * 获取link验证token
     *
     * @return link验证token，未设置时返回null
     */
    public static String getLink() {
        return link.get();
    }


    /**
     * 清除当前线程的用户信息，防止内存泄漏
     */
    public static void clear() {
        uid.remove();
        link.remove();
    }
}
