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

    IPage<CommonUserInfoResponseVO> pageUsers(CommonUserPageQueryDTO query);

    CommonUserInfoResponseVO getOneById(Long uid);

    void updateCommonUserStatus( com.yulgnier.center.user.api.model.dto.CommonUserStatusUpdateRequestDTO request);
}

