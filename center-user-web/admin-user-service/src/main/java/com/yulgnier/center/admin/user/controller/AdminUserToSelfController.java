package com.yulgnier.center.admin.user.controller;

import com.yulgnier.center.admin.user.model.dto.*;
import com.yulgnier.center.admin.user.model.vo.AdminUserInfoResponseVO;
import com.yulgnier.center.admin.user.model.vo.AdminUserLoginResponseVO;
import com.yulgnier.center.admin.user.service.AdminUserService;
import com.yulgnier.center.user.api.model.dto.EmailCodeRequestDTO;
import com.yulgnier.common.model.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/center-admin/userself")
@Tag(name = "管理员对接自己主账号", description = "主要是管理员自己对自己账号的接口")
public class AdminUserToSelfController {
    private final AdminUserService userService;
    @Operation(summary = "获取邮箱验证码")
    @PostMapping("/getEmailCode")
    public Result<String> getEmailCode(@Valid @RequestBody EmailCodeRequestDTO request) {
        String response = userService.getEmailCode(request);
        return Result.ok(response);
    }

    @Operation(summary = "找回密码")
    @PostMapping("/forgetPassword")
    public Result<String> forgetPassword(@Valid @RequestBody UserForgetPasswordRequestDTO request) {
        String response = userService.forgetPassword(request);
        return Result.ok(response);
    }

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<String> login( @Valid @RequestBody UserLoginRequestDTO request) {
        AdminUserLoginResponseVO response = userService.login(request);
        return Result.ok(response.getResultCodeENum(),response.getToken());
    }

    @Operation(summary = "换绑邮箱")
    @PostMapping("/changeEmail")
    public Result<String> changeEmail(@Valid @RequestBody UserChangeEmailRequestDTO request) {
        userService.changeEmail(request);
        return Result.ok("更改成功");
    }

    @Operation(summary = "修改用户普通信息")
    @PostMapping("/updateUserInfo")
    public Result<String> updateUserInfo(@Valid @RequestBody UserUpdateInfoRequestDTO request) {
        userService.updateUserInfo(request);
        return Result.ok("修改成功");
    }
    @Operation(summary = "获取用户基础信息")
    @GetMapping("/getUserInfo")
    public Result<AdminUserInfoResponseVO> getUserInfo() {
        AdminUserInfoResponseVO response = userService.getUserInfo();
        return Result.ok(response);
    }
    @Operation(summary = "修改用户密码")
    @PostMapping("/updatePassword")
    public Result<String> updatePassword(@Valid @RequestBody UserUpdatePasswordRequestDTO request) {
        userService.updatePassword(request);
        return Result.ok("修改成功");
    }
    @Operation(summary = "管理员用户注销")
    @PostMapping("/logout")
    public Result<String> logout(@Valid @RequestBody AdminUserLogoutRequestDTO request) {
        userService.logout(request);
        return Result.ok("注销成功");
    }
}
