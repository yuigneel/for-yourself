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
import com.yuigneel.center.admin.user.service.AdminUserService;
import com.yuigneel.center.user.api.model.dto.AccountStatusUpdateRequestDTO;
import com.yuigneel.center.user.api.model.dto.CommonUserPageQueryDTO;
import com.yuigneel.center.user.api.model.vo.CommonUserInfoResponseVO;
import com.yuigneel.common.model.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员对普通用户接口控制器
 * <p>
 * 处理管理员对普通用户进行管理的核心业务接口，包括分页查询、获取用户信息、状态管理等
 * </p>
 *
 * @author 羽·伊格尼尔
 * @since 2026-05-06
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/center-admin/user-to-common")
@Tag(name = "管理员对普通用户接口", description = "主要是管理员对普通用户进行管理")
public class AdminUserToOtherCommonController {

    private final AdminUserService adminUserService;


    /**
     * 分页查询普通用户列表
     *
     * @param query 查询参数
     * @return 分页结果
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询普通用户列表", description = "通过 Feign 调用普通用户服务")
    public Result<IPage<CommonUserInfoResponseVO>> pageCommonUsers(CommonUserPageQueryDTO query) {
        IPage<CommonUserInfoResponseVO> page = adminUserService.getCommonPages(query);
        return Result.ok(page);
    }

    /**
     * 获取单个普通用户信息
     *
     * @param uid 普通用户UID
     * @return 普通用户详细信息
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    @GetMapping("/get")
    @Operation(summary = "获取单个普通用户信息", description = "通过 Feign 调用普通用户服务")
    public Result<CommonUserInfoResponseVO> getCommonUserInfo(@Parameter(name = "uid", description = "普通用户UID（雪花ID）", required = true) @RequestParam("uid") @NotNull(message = "普通用户UID不能为空") Long uid) {
        CommonUserInfoResponseVO info = adminUserService.getOneCommonUserInfo(uid);
        return Result.ok(info);
    }

    /**
     * 修改普通用户账号状态
     *
     * @param request 账号状态更新请求参数
     * @return 操作结果提示
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    @PostMapping("/updateStatus")
    @Operation(summary = "修改普通用户账号状态", description = "通过 Feign 调用普通用户服务")
    public Result<String> updateCommonUserStatus(@Valid @RequestBody AccountStatusUpdateRequestDTO request){
        adminUserService.updateCommonUserStatus(request);
        return Result.ok();
    }
}

