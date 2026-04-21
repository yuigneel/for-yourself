package com.yulgnier.center.admin.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yulgnier.center.admin.user.model.dto.AdminUserPageQueryDTO;
import com.yulgnier.center.admin.user.model.vo.AdminUserInfoResponseVO;
import com.yulgnier.center.admin.user.service.AdminUserService;
import com.yulgnier.center.user.api.client.CommonUserClient;
import com.yulgnier.center.user.api.model.dto.CommonUserPageQueryDTO;
import com.yulgnier.center.user.api.model.vo.CommonUserInfoResponseVO;
import com.yulgnier.common.model.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/center-admin/user-to-admin")
@Tag(name = "管理员对其它管理员", description = "主要是管理员管理其它管理员的接口")
public class AdminUserToOtherAdminController {

    private final AdminUserService adminUserService;
    private final CommonUserClient commonUserClient;

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
     * 分页查询普通用户列表
     * <p>通过 Feign 调用 common-user-service，无需额外权限校验（Gateway 已确保是管理员）</p>
     *
     * @param query 查询参数
     * @return 分页结果
     */
    @GetMapping("/common-users/page")
    @Operation(summary = "分页查询普通用户列表", description = "通过 Feign 调用普通用户服务")
    public Result<IPage<CommonUserInfoResponseVO>> pageCommonUsers(CommonUserPageQueryDTO query) {
        IPage<CommonUserInfoResponseVO> page = commonUserClient.pageUsers(query).getData();
        return Result.ok(page);
    }
}
