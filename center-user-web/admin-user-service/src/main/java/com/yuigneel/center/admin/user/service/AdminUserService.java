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
 * 管理员用户服务接口
 * <p>
 * 提供管理员用户的注册、登录、信息管理、权限管理、状态管理等核心业务功能
 * </p>
 *
 * @author 羽·伊格尼尔
 * @since 2026-05-06
 */
public interface AdminUserService extends IService<AdminUser> {

    /**
     * 获取邮箱验证码
     *
     * @param request 邮箱验证码请求参数
     * @return 操作结果提示
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    String getEmailCode( EmailCodeRequestDTO request);

    /**
     * 找回密码
     *
     * @param request 找回密码请求参数
     * @return 新生成的密码
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    String forgetPassword(UserForgetPasswordRequestDTO request);

    /**
     * 管理员登录
     *
     * @param request 登录请求参数
     * @return 登录结果及令牌
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    AdminUserLoginResponseVO login( UserLoginRequestDTO request);

    /**
     * 换绑邮箱
     *
     * @param request 换绑邮箱请求参数
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    void changeEmail(UserChangeEmailRequestDTO request);

    /**
     * 更新管理员个人信息
     *
     * @param request 用户信息更新请求参数
     * @param avatarFile 头像文件
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    void updateUserInfo( UserUpdateInfoRequestDTO request, MultipartFile avatarFile);

    /**
     * 获取当前管理员的个人信息
     *
     * @return 管理员详细信息
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    AdminUserInfoResponseVO getAdminSelfInfo();

    /**
     * 修改管理员密码
     *
     * @param request 密码修改请求参数
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    void updatePassword( UserUpdatePasswordRequestDTO request);

    /**
     * 管理员注销
     *
     * @param request 注销请求参数
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    void logout( AdminUserLogoutRequestDTO request);

    /**
     * 分页查询管理员列表
     *
     * @param query 查询参数
     * @return 分页结果
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    IPage<AdminUserInfoResponseVO> pageUsers(AdminUserPageQueryDTO query);

    /**
     * 创建新的管理员账号
     *
     * @param request 创建请求参数
     * @return 创建的账户信息，包含昵称和初始密码
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    AdminUserCreateResponseVO createAdmin(AdminUserCreateRequestDTO request);

    /**
     * 修改管理员权限等级
     *
     * @param request 权限更新请求参数
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    void updateAdminPermission(AdminUserPermissionUpdateRequestDTO request);

    /**
     * 分页查询普通用户列表
     *
     * @param query 查询参数
     * @return 分页结果
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    IPage<CommonUserInfoResponseVO> getCommonPages(CommonUserPageQueryDTO query);

    /**
     * 修改管理员账号状态
     *
     * @param request 账号状态更新请求参数
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    void updateAdminStatus(AccountStatusUpdateRequestDTO request );

    /**
     * 获取指定管理员的详细信息
     *
     * @param uid 管理员UID
     * @return 管理员详细信息
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    AdminUserInfoResponseVO getOtherAdminInfo(Long uid);

    /**
     * 获取指定普通用户的详细信息
     *
     * @param uid 普通用户UID
     * @return 普通用户详细信息
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    CommonUserInfoResponseVO getOneCommonUserInfo( Long uid);

    /**
     * 修改普通用户账号状态
     *
     * @param request 账号状态更新请求参数
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    void updateCommonUserStatus(AccountStatusUpdateRequestDTO request);

    /**
     * 获取新的JWT令牌
     *
     * @return 新的JWT令牌
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    String getNewJWT();
}

