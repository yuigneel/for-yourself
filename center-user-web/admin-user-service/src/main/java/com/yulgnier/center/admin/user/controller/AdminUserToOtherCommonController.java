package com.yulgnier.center.admin.user.controller;

import com.yulgnier.center.admin.user.service.AdminUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/center-admin/user-to-common")
@Tag(name = "管理员对普通用户", description = "主要是管理员对普通用户进行管理")
public class AdminUserToOtherCommonController {
    private final AdminUserService adminUserService;
}
