package com.yuigneel.center.common.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuigneel.center.common.user.model.dto.*;
import com.yuigneel.center.common.user.model.domain.CommonUser;
import com.yuigneel.center.common.user.model.vo.UserLoginResponseVO;
import com.yuigneel.center.user.api.model.dto.AccountStatusUpdateRequestDTO;
import com.yuigneel.center.user.api.model.dto.CommonUserPageQueryDTO;
import com.yuigneel.center.user.api.model.dto.EmailCodeRequestDTO;
import com.yuigneel.center.user.api.model.vo.CommonUserInfoResponseVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 普通用户服务接口
 * <p>
 * 提供普通用户的注册、登录、信息管理、密码修改等核心业务功能
 * </p>
 *
 * @author 逆羽风辰
 * @since 2026-05-06
 */
public interface UserService extends IService<CommonUser> {

    /**
     * 获取邮箱验证码
     *
     * @param request 邮箱验证码请求参数
     * @return 操作结果提示
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    String getEmailCode(EmailCodeRequestDTO request);

    /**
     * 用户注册
     *
     * @param request 注册请求参数
     * @param avatarFile 头像文件
     * @return JWT令牌
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    String register(UserRegisterRequestDTO request, MultipartFile  avatarFile);

    /**
     * 用户登录
     *
     * @param request 登录请求参数
     * @return 登录结果及令牌
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    UserLoginResponseVO login(UserLoginRequestDTO request);

    /**
     * 用户注销
     *
     * @param request 注销请求参数
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    void cancel(UserCancelRequestDTO request);

    /**
     * 找回密码
     *
     * @param request 找回密码请求参数
     * @return 新生成的密码
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    String forgetPassword(UserForgetPasswordRequestDTO request);

    /**
     * 更新用户个人信息
     *
     * @param request 用户信息更新请求参数
     * @param avatarFile 头像文件
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    void updateUserInfo(UserUpdateInfoRequestDTO request, MultipartFile avatarFile);

    /**
     * 换绑邮箱
     *
     * @param request 换绑邮箱请求参数
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    void changeEmail(UserChangeEmailRequestDTO request);

    /**
     * 获取当前用户的个人信息
     *
     * @return 用户详细信息
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    CommonUserInfoResponseVO getUserInfo();

    /**
     * 修改用户密码
     *
     * @param request 密码修改请求参数
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    void updatePassword( UserUpdatePasswordRequestDTO request);

    /**
     * 分页查询普通用户列表
     *
     * @param query 查询参数
     * @return 分页结果
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    IPage<CommonUserInfoResponseVO> pageUsers(CommonUserPageQueryDTO query);

    /**
     * 根据UID获取普通用户信息
     *
     * @param uid 用户UID
     * @return 用户详细信息
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    CommonUserInfoResponseVO getOneById(Long uid);

    /**
     * 修改普通用户账号状态
     *
     * @param request 账号状态更新请求参数
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    void updateCommonUserStatus( AccountStatusUpdateRequestDTO request);

    /**
     * 获取新的JWT令牌
     *
     * @return 新的JWT令牌
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    String getNewJWT();
}

