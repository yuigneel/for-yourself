package com.yulgnier.center.admin.user.service;

import com.yulgnier.center.admin.user.model.domain.AdminUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yulgnier.center.admin.user.model.dto.*;
import com.yulgnier.center.admin.user.model.vo.AdminUserInfoResponseVO;
import com.yulgnier.center.admin.user.model.vo.AdminUserLoginResponseVO;
import jakarta.validation.Valid;

/**
* @author Yu_Lgnier
* @description 针对表【t_admin_user(管理员用户基础信息表)】的数据库操作Service
* @createDate 2026-04-18 16:37:15
*/
public interface AdminUserService extends IService<AdminUser> {

    String getEmailCode( EmailCodeRequestDTO request);

    String forgetPassword( UserForgetPasswordRequestDTO request);

    AdminUserLoginResponseVO login(@Valid UserLoginRequestDTO request);

    void changeEmail(@Valid UserChangeEmailRequestDTO request);

    void updateUserInfo(@Valid UserUpdateInfoRequestDTO request);

    AdminUserInfoResponseVO getUserInfo();

    void updatePassword(@Valid UserUpdatePasswordRequestDTO request);
}
