package com.yuigneel.center.admin.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yuigneel.center.admin.user.model.domain.AdminUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuigneel.center.admin.user.model.dto.*;
import com.yuigneel.center.user.api.model.dto.AccountStatusUpdateRequestDTO;
import com.yuigneel.center.admin.user.model.vo.AdminUserCreateResponseVO;
import com.yuigneel.center.admin.user.model.vo.AdminUserInfoResponseVO;
import com.yuigneel.center.admin.user.model.vo.AdminUserLoginResponseVO;
import com.yuigneel.center.user.api.model.dto.CommonUserPageQueryDTO;
import com.yuigneel.center.user.api.model.dto.EmailCodeRequestDTO;
import com.yuigneel.center.user.api.model.vo.CommonUserInfoResponseVO;
import org.springframework.web.multipart.MultipartFile;


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

    void updateUserInfo( UserUpdateInfoRequestDTO request, MultipartFile avatarFile);

    AdminUserInfoResponseVO getAdminSelfInfo();

    void updatePassword( UserUpdatePasswordRequestDTO request);

    void logout( AdminUserLogoutRequestDTO request);

    IPage<AdminUserInfoResponseVO> pageUsers(AdminUserPageQueryDTO query);

    AdminUserCreateResponseVO createAdmin(AdminUserCreateRequestDTO request);

    void updateAdminPermission(AdminUserPermissionUpdateRequestDTO request);

    IPage<CommonUserInfoResponseVO> getCommonPages(CommonUserPageQueryDTO query);

    void updateAdminStatus( AccountStatusUpdateRequestDTO request);

    AdminUserInfoResponseVO getOtherAdminInfo(Long uid);

    CommonUserInfoResponseVO getOneCommonUserInfo( Long uid);

    void updateCommonUserStatus(AccountStatusUpdateRequestDTO request);

    String getNewJWT();
}

