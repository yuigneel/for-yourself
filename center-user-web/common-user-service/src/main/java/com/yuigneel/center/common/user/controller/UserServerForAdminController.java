package com.yuigneel.center.common.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yuigneel.center.common.user.service.CommonUserFileService;
import com.yuigneel.center.user.api.model.dto.CommonUserStatusUpdateRequestDTO;
import com.yuigneel.center.common.user.service.UserService;
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
     * <p>仅管理员可调用，内部会校验身份</p>
     *
     * @param query 查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询普通用户列表", description = "仅管理员可调用，支持时间范围、状态、关键词等条件筛选")
    public Result<IPage<CommonUserInfoResponseVO>> pageUsers(CommonUserPageQueryDTO query) {
        IPage<CommonUserInfoResponseVO> page = userService.pageUsers(query);
        return Result.ok(page);
    }

    /**
     * 根据id获得普通用户信息
     * <p>仅管理员可调用，内部会校验身份</p>
     */
    @GetMapping("/getById")
    @Operation(summary = "根据id获得普通用户信息")
    public Result<CommonUserInfoResponseVO> getById(@Parameter(name = "uid", description = "普通用户UID（雪花ID）", required = true) @RequestParam("uid") @NotNull(message = "普通用户UID不能为空") Long uid) {
        CommonUserInfoResponseVO response = userService.getOneById(uid);
        // 将管理员Id 替换为用户Id
        UserContextUtil.setUid(response.getUid());
        String avatar = commonUserFileServiceByMinIOImpl.getAvatar();
        response.setAvatar(avatar);
        return Result.ok(response);
    }

    /**
     * 更改普通用户的账号状态
     * <p>仅管理员可调用，内部会校验身份</p>
     *
     * @param request 状态更新请求参数，包含普通用户UID和新的账户状态
     * @return 操作结果提示
     */
    @PostMapping("/changeStatus")
    @Operation(summary = "更改普通用户的账号状态", description = "仅管理员可调用，内部会校验身份")
    public Result<String> changeStatus(@Valid @RequestBody CommonUserStatusUpdateRequestDTO request) {
        userService.updateCommonUserStatus(request);
        return Result.ok("修改状态成功！");
    }
}
