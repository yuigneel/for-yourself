package com.yulgnier.center.admin.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yulgnier.center.admin.user.config.properties.LinkProperties;
import com.yulgnier.center.admin.user.service.AdminUserService;
import com.yulgnier.center.user.api.client.CommonUserClient;
import com.yulgnier.center.user.api.model.dto.CommonUserPageQueryDTO;
import com.yulgnier.center.user.api.model.vo.CommonUserInfoResponseVO;
import com.yulgnier.common.model.result.Result;
import com.yulgnier.common.utils.InnerFlexibleTokenSecurityUtil;
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
@RequestMapping("/center-admin/user-to-common")
@Tag(name = "管理员对普通用户", description = "主要是管理员对普通用户进行管理")
public class AdminUserToOtherCommonController {

    private final AdminUserService adminUserService;


    /**
     * 分页查询普通用户列表
     * <p>通过 Feign 调用 common-user-service，无需额外权限校验（Gateway 已确保是管理员）</p>
     *
     * @param query 查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询普通用户列表", description = "通过 Feign 调用普通用户服务")
    public Result<IPage<CommonUserInfoResponseVO>> pageCommonUsers(CommonUserPageQueryDTO query) {
        IPage<CommonUserInfoResponseVO> page = adminUserService.getCommonPages(query);
        return Result.ok(page);
    }
}

