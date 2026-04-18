package com.yulgnier.center.common.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yulgnier.center.common.user.model.domain.CommonUser;
import com.yulgnier.center.common.user.model.dto.*;
import com.yulgnier.center.common.user.model.vo.UserInfoResponseVO;
import com.yulgnier.center.common.user.model.vo.UserLoginResponseVO;
import jakarta.validation.Valid;

public interface UserService extends IService<CommonUser> {
    String getEmailCode(EmailCodeRequestDTO request);

    String register(UserRegisterRequestDTO request);

    UserLoginResponseVO login(UserLoginRequestDTO request);

    void cancel(UserCancelRequestDTO request);

    String forgetPassword(UserForgetPasswordRequestDTO request);

    void updateUserInfo(UserUpdateInfoRequestDTO request);

    void changeEmail(UserChangeEmailRequestDTO request);

    UserInfoResponseVO getUserInfo();

    void updatePassword( UserUpdatePasswordRequestDTO request);
}

