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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    // 这里只是试一试这个功能，太麻烦了，个人开发就不写了
    @ApiResponses(value = {
            // 1. 【成功】发送验证码成功
            @ApiResponse(
                    responseCode = "200",
                    description = "邮箱验证码发送成功",
                    content = @Content(
                            schema = @Schema(implementation = Result.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "code": 200,
                                              "message": "操作成功",
                                              "data": "✅ 发送成功！验证码已发送至邮箱：example@email.com"
                                            }
                                            """
                            )
                    )
            ),
            // 2. 【参数校验失败】@NotBlank 触发（邮箱/验证码/人机验证为空）
            @ApiResponse(
                    responseCode = "401",
                    description = "请求参数不完整/格式错误",
                    content = @Content(
                            schema = @Schema(implementation = Result.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "code": 401,
                                              "message": "请求信息不完整",
                                              "data": null
                                            }
                                            """
                            )
                    )
            ),
            // 3. 【人机验证失败】非法请求
            @ApiResponse(
                    responseCode = "402",
                    description = "Cloudflare人机验证失败",
                    content = @Content(
                            schema = @Schema(implementation = Result.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "code": 402,
                                              "message": "非法请求",
                                              "data": "别攻击了，用爱发电，真的怕了！"
                                            }
                                            """
                            )
                    )
            ),
            // 4. 【邮箱格式错误】
            @ApiResponse(
                    responseCode = "415",
                    description = "邮箱格式不正确",
                    content = @Content(
                            schema = @Schema(implementation = Result.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "code": 415,
                                              "message": "邮箱格式不正确",
                                              "data": null
                                            }
                                            """
                            )
                    )
            ),
            // 5. 【验证码已发送，重复请求】
            @ApiResponse(
                    responseCode = "451",
                    description = "验证码已发送，请勿重复操作",
                    content = @Content(
                            schema = @Schema(implementation = Result.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "code": 451,
                                              "message": "邮箱验证码已发送，请勿重复操作",
                                              "data": null
                                            }
                                            """
                            )
                    )
            ),
            // 6. 【发送过于频繁】
            @ApiResponse(
                    responseCode = "450",
                    description = "操作频繁，请稍后重试",
                    content = @Content(
                            schema = @Schema(implementation = Result.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "code": 450,
                                              "message": "操作频繁，请稍后重试",
                                              "data": 60
                                            }
                                            """
                            )
                    )
            ),
            // 7. 【服务异常】邮件/Redis报错
            @ApiResponse(
                    responseCode = "700",
                    description = "服务异常，验证码发送失败",
                    content = @Content(
                            schema = @Schema(implementation = Result.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "code": 700,
                                              "message": "服务异常，请稍后重试",
                                              "data": null
                                            }
                                            """
                            )
                    )
            )
    })
    @Operation(summary = "获取邮箱验证码")
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
