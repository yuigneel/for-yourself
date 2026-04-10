package com.yulgnier.center.common.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yulgnier.center.common.user.model.domain.CommonUser;
import com.yulgnier.center.common.user.model.dto.EmailCodeRequestDTO;
import com.yulgnier.center.common.user.model.dto.UserLoginRequestDTO;
import com.yulgnier.center.common.user.model.dto.UserRegisterRequestDTO;

public interface UserService extends IService<CommonUser> {
    String getEmailCode(EmailCodeRequestDTO request);

    String register(UserRegisterRequestDTO request);

    String login(UserLoginRequestDTO request);
}

