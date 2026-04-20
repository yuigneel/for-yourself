package com.yulgnier.center.admin.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
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

    /**
     * 分页查询管理员用户列表
     * <p>支持条件：排除自己、时间范围、逻辑删除状态、账户权限、账户状态、关键词搜索</p>
     *
     * @param query 分页查询请求参数
     * @return 分页结果，包含管理员用户信息列表
     */
    IPage<AdminUserInfoResponseVO> pageUsers(AdminUserPageQueryDTO query);
}
