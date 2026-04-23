package com.yulgnier.center.admin.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yulgnier.center.admin.user.model.domain.AdminUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yulgnier.center.admin.user.model.dto.*;
import com.yulgnier.center.admin.user.model.vo.AdminUserCreateResponseVO;
import com.yulgnier.center.admin.user.model.vo.AdminUserInfoResponseVO;
import com.yulgnier.center.admin.user.model.vo.AdminUserLoginResponseVO;
import com.yulgnier.center.user.api.model.dto.CommonUserPageQueryDTO;
import com.yulgnier.center.user.api.model.dto.EmailCodeRequestDTO;
import com.yulgnier.center.user.api.model.vo.CommonUserInfoResponseVO;
import com.yulgnier.common.exception.ForYourselfException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


/**
* @author Yu_Lgnier
* @description 针对表【t_admin_user(管理员用户基础信息表)】的数据库操作Service
* @createDate 2026-04-18 16:37:15
*/
public interface AdminUserService extends IService<AdminUser> {

    String getEmailCode( EmailCodeRequestDTO request);

    String forgetPassword(UserForgetPasswordRequestDTO request);

    AdminUserLoginResponseVO login( UserLoginRequestDTO request);

    void changeEmail(UserChangeEmailRequestDTO request);

    void updateUserInfo( UserUpdateInfoRequestDTO request);

    AdminUserInfoResponseVO getAdminSelfInfo();

    void updatePassword( UserUpdatePasswordRequestDTO request);

    IPage<AdminUserInfoResponseVO> pageUsers(AdminUserPageQueryDTO query);

    void logout( AdminUserLogoutRequestDTO request);

    AdminUserCreateResponseVO createAdmin(AdminUserCreateRequestDTO request);

    void updateAdminPermission(AdminUserPermissionUpdateRequestDTO request);

    IPage<CommonUserInfoResponseVO> getCommonPages(CommonUserPageQueryDTO query);

    void updateAdminStatus( AdminUserStatusUpdateRequestDTO request);

    AdminUserInfoResponseVO getOtherAdminInfo(Long uid);

    CommonUserInfoResponseVO getOneCommonUserInfo( Long uid);

    void updateCommonUserStatus(com.yulgnier.center.user.api.model.dto.CommonUserStatusUpdateRequestDTO request);
}

