package com.yulgnier.center.common.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yulgnier.center.common.user.model.domain.CommonUser;
import com.yulgnier.center.common.user.model.dto.*;
import com.yulgnier.center.common.user.model.vo.UserLoginResponseVO;
import com.yulgnier.center.user.api.model.dto.CommonUserPageQueryDTO;
import com.yulgnier.center.user.api.model.dto.EmailCodeRequestDTO;
import com.yulgnier.center.user.api.model.vo.CommonUserInfoResponseVO;

public interface UserService extends IService<CommonUser> {
    String getEmailCode(EmailCodeRequestDTO request);

    String register(UserRegisterRequestDTO request);

    UserLoginResponseVO login(UserLoginRequestDTO request);

    void cancel(UserCancelRequestDTO request);

    String forgetPassword(UserForgetPasswordRequestDTO request);

    void updateUserInfo(UserUpdateInfoRequestDTO request);

    void changeEmail(UserChangeEmailRequestDTO request);

    CommonUserInfoResponseVO getUserInfo();

    void updatePassword( UserUpdatePasswordRequestDTO request);

    /**
     * 分页查询普通用户列表（仅管理员可调用）
     * <p>内部会校验调用者身份，非管理员直接拒绝</p>
     *
     * @param query 查询参数
     * @return 分页结果
     */
    IPage<CommonUserInfoResponseVO> pageUsers(CommonUserPageQueryDTO query);
}

