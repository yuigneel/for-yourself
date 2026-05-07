/*
 * For Yourself - A graduation project by the author, serving as a demonstration for the future complete project ecosystem
 * Copyright (C) 2026  Yu·Igneel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
import com.yuigneel.common.utils.UserContextUtil;
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

/**
 * 普通用户主账号接口控制器
 * <p>
 * 处理普通用户的核心业务接口，包括注册、登录、信息管理、密码修改等
 * </p>
 *
 * @author 逆羽风辰
 * @since 2026-05-06
 */
@Slf4j
@RestController
@RequestMapping("/center-common/user")
@Tag(name = "普通用户主账号接口", description = "关联其它模块的核心账号：用户登录、注册、等接口")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CommonUserFileService commonUserFileServiceByMinIOImpl;

    /**
     * 获取邮箱验证码
     *
     * @param request 邮箱验证码请求参数
     * @return 操作结果提示
     * @author 逆羽风辰
     * @since 2026-05-06
     */
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

    /**
     * 用户注册
     *
     * @param userDTO 用户注册请求参数
     * @param avatarFile 头像文件
     * @return JWT令牌
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    @Operation(summary = "用户注册")
    @PostMapping(value = "/register")
    public Result<String> register(
            @ParameterObject @Valid UserRegisterRequestDTO userDTO,      // 直接接收并验证 DTO
            @RequestPart(required = false) @Parameter(description = "用户头像文件", required = false)
            MultipartFile avatarFile) {  // 直接接收文件
        String response = userService.register(userDTO, avatarFile);
        return Result.ok(response);
    }

    /**
     * 用户登录
     *
     * @param request 登录请求参数
     * @return 登录结果及令牌
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody UserLoginRequestDTO request) {
        UserLoginResponseVO response = userService.login(request);
        LoginStatusEnum resultCodeEnum = response.getResultCodeEnum();
        String token = response.getToken();
        return Result.buildDIY(String.valueOf(ResultCodeEnum.SUCCESS.getCode()), resultCodeEnum.getName(), token);
    }

    /**
     * 用户注销
     *
     * @param request 注销请求参数
     * @return 操作结果提示
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    @Operation(summary = "用户注销")
    @PostMapping("/cancel")
    public Result<String> cancel(@Valid @RequestBody UserCancelRequestDTO request) {
        userService.cancel(request);
        return Result.ok("注销成功");
    }

    /**
     * 找回密码
     *
     * @param request 找回密码请求参数
     * @return 新生成的密码
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    @Operation(summary = "找回密码")
    @PostMapping("/forgetPassword")
    public Result<String> forgetPassword(@Valid @RequestBody UserForgetPasswordRequestDTO request) {
        String response = userService.forgetPassword(request);
        return Result.ok(response);
    }

    /**
     * 修改用户普通信息
     *
     * @param request 用户信息更新请求参数
     * @param avatarFile 头像文件
     * @return 操作结果提示
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    @Operation(summary = "修改用户普通信息")
    @PostMapping(value = "/updateUserInfo")
    public Result<String> updateUserInfo(
            @ParameterObject @Valid UserUpdateInfoRequestDTO request,
            @RequestPart(required = false) @Parameter(description = "用户头像文件", required = false)
            MultipartFile avatarFile) {
        userService.updateUserInfo(request, avatarFile);
        return Result.ok("修改成功");
    }

    /**
     * 换绑邮箱
     *
     * @param request 换绑邮箱请求参数
     * @return 操作结果提示
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    @Operation(summary = "换绑邮箱")
    @PostMapping("/changeEmail")
    public Result<String> changeEmail(@Valid @RequestBody UserChangeEmailRequestDTO request) {
        userService.changeEmail(request);
        return Result.ok("更改成功");
    }

    /**
     * 获取用户基础信息
     *
     * @return 用户详细信息
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    @Operation(summary = "获取用户基础信息")
    @GetMapping("/getUserInfo")
    public Result<CommonUserInfoResponseVO> getUserInfo() {
        CommonUserInfoResponseVO response = userService.getUserInfo();
        String avatar = commonUserFileServiceByMinIOImpl.getAvatar();
        response.setAvatar(avatar);
        return Result.ok(response);
    }

    /**
     * 修改用户密码
     *
     * @param request 密码修改请求参数
     * @return 操作结果提示
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    @Operation(summary = "修改用户密码")
    @PostMapping("/updatePassword")
    public Result<String> updatePassword(@Valid @RequestBody UserUpdatePasswordRequestDTO request) {
        userService.updatePassword(request);
        return Result.ok("修改成功");
    }

    /**
     * 上传用户头像
     *
     * @param file 图片文件
     * @return 操作结果提示
     * @author 逆羽风辰
     * @since 2026-05-06
     */
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

    /**
     * 删除用户头像
     *
     * @return 操作结果提示
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    @Operation(summary = "删除用户头像", description = "删除用户头像")
    @PostMapping("/deleteAvatar")
    public Result<String> deleteAvatar() {
        commonUserFileServiceByMinIOImpl.physicalDeleteAvatarByUid(UserContextUtil.getUid());
        return Result.ok("删除成功");
    }

    /**
     * 获取用户头像
     *
     * @return 头像临时URL
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    @Operation(summary = "获取用户头像", description = "返回用户头像的 临时URL！")
    @GetMapping("/getAvatar")
    public Result<String> getAvatar() {
        String response = commonUserFileServiceByMinIOImpl.getAvatar();
        if (response == null || response.isEmpty())
            throw new ForYourselfException(ResultCodeEnum.AVATAR_NOT_FOUND, null);
        return Result.ok(response);
    }

    /**
     * 获取新的 JWT
     *
     * @return 新的JWT令牌
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    @Operation(summary = "获取新的 JWT", description = "JWT快过期的时候前端主动请求，获取新的JWT")
    @GetMapping("/getNewJWT")
    public Result<String> getNewJWT() {
        String response = userService.getNewJWT();
        return Result.ok(response);
    }

}
