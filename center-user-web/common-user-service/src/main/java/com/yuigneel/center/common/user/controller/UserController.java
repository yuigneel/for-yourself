package com.yuigneel.center.common.user.controller;

import com.yuigneel.center.common.user.model.dto.*;
import com.yuigneel.center.common.user.model.vo.UserLoginResponseVO;
import com.yuigneel.center.common.user.service.CommonUserFileService;
import com.yuigneel.center.common.user.service.UserService;
import com.yuigneel.center.user.api.model.dto.EmailCodeRequestDTO;
import com.yuigneel.center.user.api.model.enums.LoginStatusEnum;
import com.yuigneel.center.user.api.model.vo.CommonUserInfoResponseVO;
import com.yuigneel.common.exception.ForYourselfException;
import com.yuigneel.common.model.result.Result;
import com.yuigneel.common.model.result.ResultCodeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
@RequestMapping("/center-common/user")
@Tag(name = "普通用户主账号接口", description = "关联其它模块的核心账号：用户登录、注册、等接口")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CommonUserFileService commonUserFileServiceByMinIOImpl;

    @Operation(
            summary = "获取邮箱验证码",
            description = "发送邮箱验证码用于注册、找回密码等操作，需要完成 Cloudflare 人机验证",
            responses = {
                    // 1. 【成功】发送验证码成功
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "邮箱验证码发送成功",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Result.class),
                                    examples = @ExampleObject(
                                            name = "发送成功",
                                            summary = "验证码已成功发送至邮箱",
                                            value = """
                                                    {
                                                      "code": "00000",
                                                      "message": "操作成功",
                                                      "data": "✅ 发送成功！验证码已发送至邮箱：example@email.com"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    // 2. 【参数校验失败】@NotBlank 触发（邮箱/验证码/人机验证为空）
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "请求参数不完整或格式错误",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Result.class),
                                    examples = @ExampleObject(
                                            name = "参数校验失败",
                                            summary = "请求信息不完整",
                                            value = """
                                                    {
                                                      "code": "A0200",
                                                      "message": "请求参数不完整",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    // 3. 【人机验证失败】非法请求
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Cloudflare 人机验证失败",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Result.class),
                                    examples = @ExampleObject(
                                            name = "人机验证失败",
                                            summary = "非法请求",
                                            value = """
                                                    {
                                                      "code": "A0100",
                                                      "message": "人机验证失败",
                                                      "data": "别攻击了，用爱发电，真的怕了！"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    // 4. 【邮箱格式错误】
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "邮箱格式不正确",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Result.class),
                                    examples = @ExampleObject(
                                            name = "邮箱格式错误",
                                            summary = "邮箱格式不正确",
                                            value = """
                                                    {
                                                      "code": "A0203",
                                                      "message": "邮箱格式错误",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    // 5. 【验证码已发送，重复请求】
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "验证码已发送，请勿重复操作",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Result.class),
                                    examples = @ExampleObject(
                                            name = "重复请求",
                                            summary = "验证码已发送",
                                            value = """
                                                    {
                                                      "code": "A0102",
                                                      "message": "验证码已发送",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    // 6. 【发送过于频繁】
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "429",
                            description = "操作频繁，请稍后重试",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Result.class),
                                    examples = @ExampleObject(
                                            name = "请求过于频繁",
                                            summary = "操作频繁，请稍后重试",
                                            value = """
                                                    {
                                                      "code": "A0101",
                                                      "message": "验证码请求过于频繁",
                                                      "data": 60
                                                    }
                                                    """
                                    )
                            )
                    ),
                    // 7. 【服务异常】邮件/Redis报错
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "服务异常，验证码发送失败",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Result.class),
                                    examples = @ExampleObject(
                                            name = "服务异常",
                                            summary = "服务异常，请稍后重试",
                                            value = """
                                                    {
                                                      "code": "C0400",
                                                      "message": "通知服务出错",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @PostMapping("/getEmailCode")
    public Result<String> getEmailCode(@Valid @RequestBody EmailCodeRequestDTO request) {
        String response = userService.getEmailCode(request);
        return Result.ok(response);
    }

    @Operation(summary = "用户注册")
    @PostMapping(value = "/register")
    public Result<String> register(
            @ParameterObject @Valid UserRegisterRequestDTO userDTO,      // 直接接收并验证 DTO
            @RequestPart(required = false) @Parameter(description = "用户头像文件", required = false)
            MultipartFile avatarFile) {  // 直接接收文件
        String response = userService.register(userDTO, avatarFile);
        return Result.ok(response);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody UserLoginRequestDTO request) {
        UserLoginResponseVO response = userService.login(request);
        LoginStatusEnum resultCodeEnum = response.getResultCodeEnum();
        String token = response.getToken();
        return Result.buildDIY(String.valueOf(ResultCodeEnum.SUCCESS.getCode()), resultCodeEnum.getName(), token);
    }

    @Operation(summary = "用户注销")
    @PostMapping("/cancel")
    public Result<String> cancel(@Valid @RequestBody UserCancelRequestDTO request) {
        userService.cancel(request);
        return Result.ok("注销成功");
    }

    @Operation(summary = "找回密码")
    @PostMapping("/forgetPassword")
    public Result<String> forgetPassword(@Valid @RequestBody UserForgetPasswordRequestDTO request) {
        String response = userService.forgetPassword(request);
        return Result.ok(response);
    }

    @Operation(summary = "修改用户普通信息")
    @PostMapping(value = "/updateUserInfo")
    public Result<String> updateUserInfo(
            @ParameterObject @Valid UserUpdateInfoRequestDTO request,
            @RequestPart(required = false) @Parameter(description = "用户头像文件", required = false)
            MultipartFile avatarFile) {
        userService.updateUserInfo(request, avatarFile);
        return Result.ok("修改成功");
    }

    @Operation(summary = "换绑邮箱")
    @PostMapping("/changeEmail")
    public Result<String> changeEmail(@Valid @RequestBody UserChangeEmailRequestDTO request) {
        userService.changeEmail(request);
        return Result.ok("更改成功");
    }

    @Operation(summary = "获取用户基础信息")
    @GetMapping("/getUserInfo")
    public Result<CommonUserInfoResponseVO> getUserInfo() {
        CommonUserInfoResponseVO response = userService.getUserInfo();
        String avatar = commonUserFileServiceByMinIOImpl.getAvatar();
        response.setAvatar(avatar);
        return Result.ok(response);
    }

    @Operation(summary = "修改用户密码")
    @PostMapping("/updatePassword")
    public Result<String> updatePassword(@Valid @RequestBody UserUpdatePasswordRequestDTO request) {
        userService.updatePassword(request);
        return Result.ok("修改成功");
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
        commonUserFileServiceByMinIOImpl.uploadAvatar(file);
        return Result.ok("头像上传成功！");
    }

    @Operation(summary = "获取用户头像", description = "返回用户头像的 临时URL！")
    @GetMapping("/getAvatar")
    public Result<String> getAvatar() {
        String response = commonUserFileServiceByMinIOImpl.getAvatar();
        if (response == null || response.isEmpty())
            throw new ForYourselfException(ResultCodeEnum.AVATAR_NOT_FOUND, null);
        return Result.ok(response);
    }
}
