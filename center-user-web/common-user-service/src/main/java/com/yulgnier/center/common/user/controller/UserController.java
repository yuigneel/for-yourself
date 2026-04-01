package com.yulgnier.center.common.user.controller;

import com.yulgnier.center.common.user.model.dto.EmailCodeRequestDTO;
import com.yulgnier.center.common.user.model.dto.UserRegisterRequestDTO;
import com.yulgnier.center.common.user.service.UserService;
import com.yulgnier.common.exception.ForYourselfException;
import com.yulgnier.common.model.result.Result;
import com.yulgnier.common.model.result.ResultCodeEnum;
import com.yulgnier.common.utils.ValidateUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/center-common/user")
@Tag(name = "普通用户主账号", description = "关联其它模块的核心账号：用户登录、注册、等接口")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public String register(@RequestBody UserRegisterRequestDTO request) {
        log.info("用户{}申请注册", request.getNickname());
        return null;
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public String login() {
        return "登录成功";
    }

    @Operation(summary = "用户注销")
    @PostMapping("/cancel")
    public String cancel() {
        return "注销成功";
    }

    @Operation(summary = "获取邮箱验证码")
    @PostMapping("/getEmailCode")
    public Result<String> getEmailCode(@RequestBody EmailCodeRequestDTO request) {
        log.info("用户申请获取邮箱验证码：email={}, businessType={}", request.getEmail(), request.getBusinessType());
        String result = userService.getEmailCode(request);
        return Result.ok(result);
    }

    @Operation(summary = "找回密码")
    @PostMapping("/forgetPassword")
    public String forgetPassword() {
        return "找回成功";
    }

    @Operation(summary = "修改用户信息")
    @PostMapping("/updateUserInfo")
    public String updateUserInfo() {
        return "修改成功";
    }
}
