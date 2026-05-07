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
package com.yuigneel.center.admin.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yuigneel.center.admin.user.model.dto.AdminUserCreateRequestDTO;
import com.yuigneel.center.admin.user.model.dto.AdminUserPageQueryDTO;
import com.yuigneel.center.admin.user.model.dto.AdminUserPermissionUpdateRequestDTO;
import com.yuigneel.center.admin.user.model.vo.AdminUserCreateResponseVO;
import com.yuigneel.center.admin.user.model.vo.AdminUserInfoResponseVO;
import com.yuigneel.center.admin.user.service.AdminUserFileService;
import com.yuigneel.center.admin.user.service.AdminUserService;
import com.yuigneel.center.user.api.model.dto.AccountStatusUpdateRequestDTO;
import com.yuigneel.common.model.result.Result;
import com.yuigneel.common.utils.UserContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员对其它管理员接口控制器
 * <p>
 * 处理管理员管理其它管理员的核心业务接口，包括分页查询、创建账号、权限修改、状态管理等
 * </p>
 *
 * @author Yu·Igneel
 * @since 2026-05-06
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/center-admin/user-to-admin")
@Tag(name = "管理员对其它管理员接口", description = "主要是管理员管理其它管理员的接口")
public class AdminUserToOtherAdminController {

    private final AdminUserService adminUserService;
    private final AdminUserFileService adminUserFileServiceByMinIOImpl;

    /**
     * 分页查询管理员用户列表
     *
     * @param query 查询参数
     * @return 分页结果
     * @author Yu·Igneel
     * @since 2026-05-06
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询管理员列表", description = "支持排除自己、时间范围、权限、状态、删除状态、关键词等条件筛选")
    public Result<IPage<AdminUserInfoResponseVO>> pageUsers(AdminUserPageQueryDTO query) {
        IPage<AdminUserInfoResponseVO> page = adminUserService.pageUsers(query);
        return Result.ok(page);
    }

    /**
     * 创建新的管理员账号
     *
     * @param request 创建请求参数，包含邮箱和权限等级
     * @return 创建的账户信息，包含账户昵称和原始密码
     * @author Yu·Igneel
     * @since 2026-05-06
     */
    @PostMapping("/create")
    @Operation(summary = "创建管理员账号", description = "创建新的管理员账号，系统自动生成昵称和初始密码")
    public Result<AdminUserCreateResponseVO> createAdmin(@Valid @RequestBody AdminUserCreateRequestDTO request) {
        AdminUserCreateResponseVO response = adminUserService.createAdmin(request);
        return Result.ok(response);
    }

    /**
     * 修改其它管理员的权限等级
     *
     * @param request 权限更新请求参数
     * @return 操作结果提示
     * @author Yu·Igneel
     * @since 2026-05-06
     */
    @PostMapping("/update-permission")
    @Operation(summary = "修改其它管理员权限等级", description = "修改其它管理员的权限等级")
    public Result<String> updateAdminPermission(@Valid @RequestBody AdminUserPermissionUpdateRequestDTO request) {
        adminUserService.updateAdminPermission(request);
        return Result.ok("修改权限成功！");
    }

    /**
     * 修改其它管理员的账号状态
     *
     * @param request 账号状态更新请求参数
     * @return 操作结果提示
     * @author Yu·Igneel
     * @since 2026-05-06
     */
    @PostMapping("/update-status")
    @Operation(summary = "修改其它管理员账号状态", description = "修改其它管理员的账号状态")
    public Result<String> updateAdminStatus(@Valid @RequestBody AccountStatusUpdateRequestDTO request ) {
        adminUserService.updateAdminStatus(request);
        return Result.ok("修改状态成功！");
    }

    /**
     * 获取特定某个管理员的信息
     *
     * @param uid 管理员UID
     * @return 管理员详细信息
     * @author Yu·Igneel
     * @since 2026-05-06
     */
    @GetMapping("/get-info")
    @Operation(summary = "获取特定某个管理员的信息", description = "获取特定某个管理员的信息")
    public Result<AdminUserInfoResponseVO> getUserInfo(
            // 核心：@Parameter = 单个参数的文档提示（替代@Schema）
            @Parameter(
                    name = "uid",
                    description = "管理员UID（雪花ID）",
                    required = true
            )
            // 接收参数
            @RequestParam("uid")
            // 参数校验
            @NotNull(message = "管理员 UID 不能为空")
            Long uid
    ) {
        AdminUserInfoResponseVO response = adminUserService.getOtherAdminInfo(uid);
        UserContextUtil.setUid(uid);
        String avatar = adminUserFileServiceByMinIOImpl.getAvatar();
        response.setAvatar(avatar);
        return Result.ok(response);
    }
}
