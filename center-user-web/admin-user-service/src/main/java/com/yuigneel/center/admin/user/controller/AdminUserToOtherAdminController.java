package com.yuigneel.center.admin.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yuigneel.center.admin.user.model.dto.AdminUserCreateRequestDTO;
import com.yuigneel.center.admin.user.model.dto.AdminUserPageQueryDTO;
import com.yuigneel.center.admin.user.model.dto.AdminUserPermissionUpdateRequestDTO;
import com.yuigneel.center.admin.user.model.dto.AdminUserStatusUpdateRequestDTO;
import com.yuigneel.center.admin.user.model.vo.AdminUserCreateResponseVO;
import com.yuigneel.center.admin.user.model.vo.AdminUserInfoResponseVO;
import com.yuigneel.center.admin.user.service.AdminUserService;
import com.yuigneel.common.model.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/center-admin/user-to-admin")
@Tag(name = "管理员对其它管理员接口", description = "主要是管理员管理其它管理员的接口")
public class AdminUserToOtherAdminController {

    private final AdminUserService adminUserService;

    /**
     * 分页查询管理员用户列表
     * <p>支持条件：排除自己、时间范围、逻辑删除状态、账户权限、账户状态、关键词搜索</p>
     *
     * @param query 查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询管理员列表", description = "支持排除自己、时间范围、权限、状态、删除状态、关键词等条件筛选")
    public Result<IPage<AdminUserInfoResponseVO>> pageUsers(AdminUserPageQueryDTO query) {
        IPage<AdminUserInfoResponseVO> page = adminUserService.pageUsers(query);
        return Result.ok(page);
    }

    /**
     * 创建新的管理员账号
     * <p>系统会自动生成账户昵称和原始密码，并在响应中返回</p>
     *
     * @param request 创建请求参数，包含邮箱和权限等级
     * @return 创建的账户信息，包含账户昵称和原始密码
     */
    @PostMapping("/create")
    @Operation(summary = "创建管理员账号", description = "创建新的管理员账号，系统自动生成昵称和初始密码")
    public Result<AdminUserCreateResponseVO> createAdmin(@Valid @RequestBody AdminUserCreateRequestDTO request) {
        AdminUserCreateResponseVO response = adminUserService.createAdmin(request);
        return Result.ok(response);
    }

    /**
     * 修改其它管理员的权限等级
     */
    @PostMapping("/update-permission")
    @Operation(summary = "修改其它管理员权限等级", description = "修改其它管理员的权限等级")
    public Result<String> updateAdminPermission(@Valid @RequestBody AdminUserPermissionUpdateRequestDTO request) {
        adminUserService.updateAdminPermission(request);
        return Result.ok("修改权限成功！");
    }

    /**
     * 修改其它管理员的账号状态
     */
    @PostMapping("/update-status")
    @Operation(summary = "修改其它管理员账号状态", description = "修改其它管理员的账号状态")
    public Result<String> updateAdminStatus(@Valid @RequestBody AdminUserStatusUpdateRequestDTO request) {
        adminUserService.updateAdminStatus(request);
        return Result.ok("修改状态成功！");
    }

    /**
     * 获取特定某个管理员的信息
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
            @NotNull(message = "管理员UID不能为空")
            Long uid
    ) {
        AdminUserInfoResponseVO response = adminUserService.getOtherAdminInfo(uid);
        return Result.ok(response);
    }
}
