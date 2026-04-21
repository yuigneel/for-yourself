package com.yulgnier.center.user.api.client;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yulgnier.center.user.api.model.dto.CommonUserPageQueryDTO;
import com.yulgnier.center.user.api.model.vo.CommonUserInfoResponseVO;
import com.yulgnier.common.model.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * 普通用户服务 Feign 客户端
 * <p>供管理员模块调用，提供普通用户相关接口</p>
 *
 * @author yulgnier
 * @since 2026-04-21
 */
@FeignClient(name = "center-common-user-service", path = "/api/common-user")
public interface CommonUserClient {

    /**
     * 分页查询普通用户列表
     * <p>仅管理员可调用，内部会校验身份</p>
     *
     * @param query 查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    Result<IPage<CommonUserInfoResponseVO>> pageUsers(CommonUserPageQueryDTO query);
}
