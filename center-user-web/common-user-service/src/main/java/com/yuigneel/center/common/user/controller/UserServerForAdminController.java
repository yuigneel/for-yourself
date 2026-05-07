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
package com.yuigneel.center.common.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yuigneel.center.common.user.service.CommonUserFileService;
import com.yuigneel.center.common.user.service.UserService;
import com.yuigneel.center.user.api.model.dto.AccountStatusUpdateRequestDTO;
import com.yuigneel.center.user.api.model.dto.CommonUserPageQueryDTO;
import com.yuigneel.center.user.api.model.vo.CommonUserInfoResponseVO;
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
 * 普通用户服务管理员接口控制器
 * <p>
 * 提供服务于管理员调用的普通用户管理接口，包括分页查询、获取用户信息、状态管理等
 * </p>
 *
 * @author Yu·Igneel
 * @since 2026-05-06
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/center-common/link-admin")
@Tag(name = "普通用户服务管理员接口", description = "主要是服务于管理员接口进行调用")
public class UserServerForAdminController {

    private final UserService userService;
    private final CommonUserFileService commonUserFileServiceByMinIOImpl;

    /**
     * 分页查询普通用户列表
     *
     * @param query 查询参数
     * @return 分页结果
     * @author Yu·Igneel
     * @since 2026-05-06
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询普通用户列表", description = "仅管理员可调用，支持时间范围、状态、关键词等条件筛选")
    public Result<IPage<CommonUserInfoResponseVO>> pageUsers(CommonUserPageQueryDTO query) {
        IPage<CommonUserInfoResponseVO> page = userService.pageUsers(query);
        return Result.ok(page);
    }

    /**
     * 根据 id获得普通用户信息
     *
     * @param uid 普通用户UID
     * @return 普通用户详细信息
     * @author Yu·Igneel
     * @since 2026-05-06
     */
    @GetMapping("/getById")
    @Operation(summary = "根据 id获得普通用户信息")
    public Result<CommonUserInfoResponseVO> getById(@Parameter(name = "uid", description = "普通用户UID（雪花ID）", required = true) @RequestParam("uid") @NotNull(message = "普通用户 UID 不能为空") Long uid) {
        CommonUserInfoResponseVO response = userService.getOneById(uid);
        // 将管理员Id 替换为用户Id
        UserContextUtil.setUid(response.getUid());
        String avatar = commonUserFileServiceByMinIOImpl.getAvatar();
        response.setAvatar(avatar);
        return Result.ok(response);
    }

    /**
     * 更改普通用户的账号状态
     *
     * @param request 状态更新请求参数，包含普通用户UID和新的账户状态
     * @return 操作结果提示
     * @author Yu·Igneel
     * @since 2026-05-06
     */
    @PostMapping("/changeStatus")
    @Operation(summary = "更改普通用户的账号状态", description = "仅管理员可调用，内部会校验身份")
    public Result<String> changeStatus(@Valid @RequestBody AccountStatusUpdateRequestDTO request) {
        userService.updateCommonUserStatus(request);
        return Result.ok("修改状态成功！");
    }
}
