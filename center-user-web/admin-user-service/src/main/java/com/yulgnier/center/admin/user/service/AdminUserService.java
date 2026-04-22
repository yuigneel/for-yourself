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

    AdminUserInfoResponseVO getUserInfo();

    void updatePassword( UserUpdatePasswordRequestDTO request);

    IPage<AdminUserInfoResponseVO> pageUsers(AdminUserPageQueryDTO query);

    /**
     * 管理员用户注销（账号逻辑删除）
     * <p>业务流程：</p>
     * <ol>
     *   <li>验证 Cloudflare Turnstile 人机验证令牌</li>
     *   <li>校验用户昵称、邮箱、密码格式合法性</li>
     *   <li>从Redis中获取该邮箱对应的验证码信息（验证码+剩余尝试次数）</li>
     *   <li>验证用户输入的验证码是否正确</li>
     *   <li>验证用户密码是否与数据库中存储的密码匹配</li>
     *   <li>执行逻辑删除操作，将管理员账号标记为已注销</li>
     * </ol>
     * <p>安全机制：</p>
     * <ul>
     *   <li>验证码错误会递减剩余尝试次数，达到0次后触发邮箱冻结机制</li>
     *   <li>使用BCrypt算法验证密码，确保密码安全性</li>
     *   <li>逻辑删除而非物理删除，保留数据完整性</li>
     *   <li>注意：管理员账户被逻辑删除后，不允许像普通用户那样靠登录来恢复</li>
     * </ul>
     *
     * @param request 用户注销请求参数，包含用户昵称、邮箱、密码、验证码、Cloudflare验证响应
     * @throws ForYourselfException 当出现以下情况时抛出：
     *                              <ul>
     *                                <li>{@link com.yulgnier.common.model.result.ResultCodeEnum#CAPTCHA_VERIFICATION_FAILED} - 人机验证失败或验证码错误</li>
     *                                <li>{@link com.yulgnier.common.model.result.ResultCodeEnum#USERNAME_FORMAT_ERROR} - 用户名格式错误</li>
     *                                <li>{@link com.yulgnier.common.model.result.ResultCodeEnum#EMAIL_FORMAT_ERROR} - 邮箱格式错误</li>
     *                                <li>{@link com.yulgnier.common.model.result.ResultCodeEnum#PASSWORD_FORMAT_ERROR} - 密码格式错误</li>
     *                                <li>{@link com.yulgnier.common.model.result.ResultCodeEnum#CAPTCHA_EXPIRED} - 验证码不存在或已过期</li>
     *                                <li>{@link com.yulgnier.common.model.result.ResultCodeEnum#CACHE_SERVICE_ERROR} - Redis缓存数据格式错误或缓存服务异常</li>
     *                                <li>{@link com.yulgnier.common.model.result.ResultCodeEnum#USER_NOT_FOUND_OR_CANCELLED} - 用户不存在或已被注销</li>
     *                                <li>{@link com.yulgnier.common.model.result.ResultCodeEnum#PASSWORD_ERROR} - 密码错误</li>
     *                                <li>{@link com.yulgnier.common.model.result.ResultCodeEnum#DATABASE_SERVICE_ERROR} - 数据库服务异常</li>
     *                              </ul>
     */
    void logout( AdminUserLogoutRequestDTO request);

    AdminUserCreateResponseVO createAdmin(AdminUserCreateRequestDTO request);

    IPage<CommonUserInfoResponseVO> getCommonPages(CommonUserPageQueryDTO query);
}
