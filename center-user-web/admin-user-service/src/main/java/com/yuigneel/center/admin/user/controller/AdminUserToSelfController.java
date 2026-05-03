package com.yuigneel.center.admin.user.controller;

import com.yuigneel.center.admin.user.model.dto.*;
import com.yuigneel.center.admin.user.model.vo.AdminUserInfoResponseVO;
import com.yuigneel.center.admin.user.model.vo.AdminUserLoginResponseVO;
import com.yuigneel.center.admin.user.service.AdminUserFileService;
import com.yuigneel.center.admin.user.service.AdminUserService;
import com.yuigneel.center.user.api.model.dto.EmailCodeRequestDTO;
import com.yuigneel.common.exception.ForYourselfException;
import com.yuigneel.common.model.result.Result;
import com.yuigneel.common.model.result.ResultCodeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/center-admin/userself")
@Tag(name = "管理员对接自己主账号接口", description = "主要是管理员自己对自己账号的接口")
public class AdminUserToSelfController {
    private final AdminUserService userService;
    private final AdminUserFileService adminUserFileServiceByMinIOImpl;
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
        String message = response.getResultCodeENum().getName();
        return Result.buildDIY(ResultCodeEnum.SUCCESS.getCode(),message,response.getToken());
    }

    @Operation(summary = "换绑邮箱")
    @PostMapping("/changeEmail")
    public Result<String> changeEmail(@Valid @RequestBody UserChangeEmailRequestDTO request) {
        userService.changeEmail(request);
        return Result.ok("更改成功");
    }

    @Operation(summary = "修改管理员普通信息")
    @PostMapping("/updateUserInfo")
    public Result<String> updateUserInfo(
            @ParameterObject @Valid UserUpdateInfoRequestDTO request,
            @RequestPart(required = false) @Parameter(description = "用户头像文件", required = false)
             MultipartFile avatarFile) {
        userService.updateUserInfo(request, avatarFile);
        return Result.ok("修改成功");
    }
    @Operation(summary = "获取管理员基础信息")
    @GetMapping("/getUserInfo")
    public Result<AdminUserInfoResponseVO> getUserInfo() {
        AdminUserInfoResponseVO response = userService.getAdminSelfInfo();
        String avatar = adminUserFileServiceByMinIOImpl.getAvatar();
        response.setAvatar(avatar);
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

    @Operation(summary = "上传用户头像", description = "接收前端传来的 MultipartFile 文件，上传到 MinIO/云存储后返回提示")
    @PostMapping(value = "/uploadAvatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadAvatar(
            @Parameter(
                    name = "file",
                    description = "图片文件",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE) // 文件类型
            )
            @RequestPart("file") MultipartFile file) {   // 推荐用@RequestPart接收文件，比@RequestParam更规范
        adminUserFileServiceByMinIOImpl.uploadAvatar(file);
        return Result.ok("头像上传成功！");
    }

    @Operation(summary = "获取用户头像", description = "返回用户头像的 临时URL！")
    @GetMapping("/getAvatar")
    public Result<String> getAvatar() {
        String response = adminUserFileServiceByMinIOImpl.getAvatar();
        if (response == null || response.isEmpty())
            throw new ForYourselfException(ResultCodeEnum.AVATAR_NOT_FOUND, null);
        return Result.ok(response);
    }

    @Operation(summary = "获取新的 JWT", description = "JWT快过期的时候前端主动请求，获取新的JWT")
    @GetMapping("/getNewJWT")
    public Result<String> getNewJWT() {
        String response = userService.getNewJWT();
        return Result.ok(response);
    }
}
