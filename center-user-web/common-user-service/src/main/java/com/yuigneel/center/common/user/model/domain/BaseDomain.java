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
package com.yuigneel.center.common.user.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class BaseDomain {
    /**
     * 主键自增 ID
     */
    @TableId(type = IdType.AUTO)
    @TableField(value = "id")
    private Long id;
    /**
     * 逻辑删除：0-未删除 1-已删除（默认0）
     */
    @TableLogic // 逻辑删除 可以单独配置，也可以在application全局配置，单独>全局
    @TableField(value = "is_deleted")
    private Integer isDeleted;
    /**
     * 创建时间(自动生成)
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;
    /**
     * 更新时间(自动更新)
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}
