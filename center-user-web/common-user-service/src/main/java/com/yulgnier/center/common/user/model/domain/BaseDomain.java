package com.yulgnier.center.common.user.model.domain;

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
    /**
     * 最后更新人ID（默认null）
     */
    @TableField(value = "update_by")
    private Long updateBy;
}
