package com.yulgnier.center.admin.user.model.dto;

import com.yulgnier.common.model.enums.AccountStatusEnum;
import com.yulgnier.common.model.enums.AdminPermissionsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 管理员用户分页查询请求 DTO
 *
 * @author yulgnier
 * @date 2026-04-19
 */
@Data
@Schema(description = "管理员用户分页查询请求")
public class AdminUserPageQueryDTO {

    @Schema(description = "当前页码（从1开始）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer current = 1;

    @Schema(description = "每页条数", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer size = 10;

    @Schema(description = "是否排除当前登录用户（true-排除，false-包含）", example = "true")
    private Boolean excludeSelf = true;

    @Schema(description = "创建时间-起始日期（包含）", example = "2024-01-01")
    private LocalDate startTime;

    @Schema(description = "创建时间-截止日期（包含）", example = "2024-12-31")
    private LocalDate endTime;

    @Schema(description = "逻辑删除状态（0-未删除，1-已删除，null-全部）", example = "0")
    private Integer isDeleted;

    @Schema(description = "账户权限（null-全部）", example = "ADMIN_LEVEL_ROOT")
    private AdminPermissionsEnum accountPermission;

    @Schema(description = "账户状态（null-全部）", example = "ACCOUNT_STATUS_NORMAL")
    private AccountStatusEnum accountStatus;

    @Schema(description = "昵称或邮箱关键词（模糊查询）", example = "admin")
    private String keyword;

    @Schema(description = "排序字段（createTime-创建时间，nickname-昵称，默认createTime）", example = "createTime")
    private String orderBy = "createTime";

    @Schema(description = "排序方式（asc-升序，desc-降序，默认desc）", example = "desc")
    private String orderDirection = "desc";
}
