package com.yulgnier.center.user.api.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yulgnier.center.user.api.model.dto.CommonUserPageQueryDTO;
import com.yulgnier.center.user.api.model.dto.CommonUserStatusUpdateRequestDTO;
import com.yulgnier.center.user.api.model.vo.CommonUserInfoResponseVO;
import com.yulgnier.common.config.DefaultFeignClientConfig;
import com.yulgnier.common.model.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 普通用户链接管理员服务 Feign 客户端
 *
 * @author yulgnier
 * @since 2026-04-21
 */
@FeignClient(name = "center-common-user-service", contextId = "CommonLinkAdminClient", path = "/center-common/link-admin", configuration = DefaultFeignClientConfig.class)
public interface CommonLinkAdminClient {

    /**
     * 分页查询普通用户列表 (GET请求)
     * <p>仅管理员可调用，内部会校验身份</p>
     * <h3>⚠️ 关键注解说明：</h3>
     * <p>1. Feign默认行为：GET请求 + 【无注解对象参数】 → 自动转为 POST请求 + RequestBody(JSON)，违背HTTP规范</p>
     * <p>2. @SpringQueryMap 作用：将DTO对象的字段自动展开为 URL 查询参数，强制保持 GET 请求方式发送</p>
     *
     * @param query 分页查询条件 DTO
     * @return 分页结果
     */
    @GetMapping("/page")
    // 实际返回的是Page对象，为了反序列化无误，必须声明实现类Page而不是接口IPage
    Result<Page<CommonUserInfoResponseVO>> pageUsers(
            // 必须加此注解，否则GET会自动变POST，导致接口405/请求方式错误
            @SpringQueryMap CommonUserPageQueryDTO query);

    /**
     * 根据id获得普通用户信息
     */
    @GetMapping("/getById")
    @Operation(summary = "根据id获得普通用户信息")
    Result<CommonUserInfoResponseVO> getById(@Parameter(name = "uid", description = "普通用户UID（雪花ID）", required = true) @RequestParam("uid") @NotNull(message = "普通用户UID不能为空") Long uid);


    /**
     * 更改普通用户的账号状态
     * <p>仅管理员可调用，内部会校验身份</p>
     *
     * @param request 状态更新请求参数，包含普通用户UID和新的账户状态
     * @return 操作结果提示
     */
    @PostMapping("/changeStatus")
    @Operation(summary = "更改普通用户的账号状态", description = "仅管理员可调用，内部会校验身份")
    Result<String> changeStatus(@Valid @RequestBody CommonUserStatusUpdateRequestDTO request);

}
