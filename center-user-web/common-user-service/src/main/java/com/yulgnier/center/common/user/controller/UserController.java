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
}
