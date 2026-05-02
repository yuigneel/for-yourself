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

public interface UserService extends IService<CommonUser> {
    String getEmailCode(EmailCodeRequestDTO request);

    String register(UserRegisterRequestDTO request, MultipartFile  avatarFile);

    UserLoginResponseVO login(UserLoginRequestDTO request);

    void cancel(UserCancelRequestDTO request);

    String forgetPassword(UserForgetPasswordRequestDTO request);

    void updateUserInfo(UserUpdateInfoRequestDTO request, MultipartFile avatarFile);

    void changeEmail(UserChangeEmailRequestDTO request);

    CommonUserInfoResponseVO getUserInfo();

    void updatePassword( UserUpdatePasswordRequestDTO request);

    IPage<CommonUserInfoResponseVO> pageUsers(CommonUserPageQueryDTO query);

    CommonUserInfoResponseVO getOneById(Long uid);

    void updateCommonUserStatus( AccountStatusUpdateRequestDTO request);
}

