package com.yuigneel.center.user.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 普通用户分页查询请求 DTO
 * <p>供管理员模块调用，查询普通用户列表</p>
 *
 * @author yulgnier
 * @since 2026-04-21
 */
@Data
@Schema(description = "普通用户分页查询请求")
public class CommonUserPageQueryDTO {

    @Schema(description = "当前页码（从1开始）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer current = 1;

    @Schema(description = "每页条数", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer size = 10;

    @Schema(description = "注册时间-起始日期（包含）", example = "2024-01-01")
    private LocalDate startTime;

    @Schema(description = "注册时间-截止日期（包含）", example = "2024-12-31")
    private LocalDate endTime;

    @Schema(description = "逻辑删除状态（0-未删除，1-已删除，null-全部）", example = "0")
    private Integer isDeleted;

    @Schema(description = "账户状态（0-正常，1-警告，2-禁用，3-强制销号，null-全部）", example = "0")
    private Integer accountStatus;

    @Schema(description = "昵称或邮箱关键词（模糊查询）", example = "user")
    private String keyword;

    @Schema(description = "排序字段（createTime-注册时间，nickname-昵称，默认createTime）", example = "createTime")
    private String orderBy = "createTime";

    @Schema(description = "排序方式（asc-升序，desc-降序，默认desc）", example = "desc")
    private String orderDirection = "desc";
}
