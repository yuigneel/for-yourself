package com.yuigneel.center.admin.user.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseDomain {
    /**
     * 主键自增ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
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
