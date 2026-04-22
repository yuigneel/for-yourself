package com.yulgnier.center.admin.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yulgnier.center.admin.user.model.dto.AdminUserCreateRequestDTO;
import com.yulgnier.center.admin.user.model.dto.AdminUserPageQueryDTO;
import com.yulgnier.center.admin.user.model.vo.AdminUserCreateResponseVO;
import com.yulgnier.center.admin.user.model.vo.AdminUserInfoResponseVO;
import com.yulgnier.center.admin.user.service.AdminUserService;
import com.yulgnier.common.model.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/center-admin/user-to-admin")
@Tag(name = "管理员对其它管理员", description = "主要是管理员管理其它管理员的接口")
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
}
