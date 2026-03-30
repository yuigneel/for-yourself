package com.yulgnier.center.common.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/center-common/user/")
@Tag(name = "普通用户主账号", description = "关联其它模块的核心账号：用户登录、注册、等接口")
public class UserController {
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public String register() {
        return "注册成功";
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
    public String getEmailCode() {
        return "获取成功";
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
