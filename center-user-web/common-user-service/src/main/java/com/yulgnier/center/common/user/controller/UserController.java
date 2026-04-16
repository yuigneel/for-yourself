package com.yulgnier.center.common.user.controller;

import com.yulgnier.center.common.user.model.dto.*;
import com.yulgnier.center.common.user.service.UserService;
import com.yulgnier.common.model.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody UserRegisterRequestDTO request) {
        String response = userService.register(request);
        return Result.ok(response);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<String> login( @Valid @RequestBody UserLoginRequestDTO  request) {
        String response = userService.login(request);
        return Result.ok(response);
    }

    @Operation(summary = "用户注销")
    @PostMapping("/cancel")
    public Result<String> cancel(@Valid @RequestBody UserCancelRequestDTO request) {
        userService.cancel(request);
        return Result.ok("注销成功") ;
    }

    @Operation(summary = "找回密码")
    @PostMapping("/forgetPassword")
    public Result<String> forgetPassword(@Valid @RequestBody UserForgetPasswordRequestDTO request) {
       String response = userService.forgetPassword(request);
        return Result.ok(response);
    }
    @Operation(summary = "修改用户普通信息")
    @PostMapping("/updateUserInfo")
    public Result<String> updateUserInfo(@Valid @RequestBody UserUpdateInfoRequestDTO request) {
         userService.updateUserInfo(request);
         return Result.ok("修改成功");
    }
    @Operation(summary = "换绑邮箱")
    @PostMapping("/changeEmail")
    public String changeEmail() {
        return "换绑邮箱";
    }
}
