package com.yulgnier.center.common.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yulgnier.center.common.user.model.domain.CommonUser;
import com.yulgnier.center.common.user.service.CommonUserService;
import com.yulgnier.center.common.user.mapper.CommonUserMapper;
import org.springframework.stereotype.Service;

/**
* @author Yu_Lgnier
* @description 针对表【t_common_user(普通用户基础信息表)】的数据库操作Service实现
* @createDate 2026-03-30 13:04:36
*/
@Service
public class CommonUserServiceImpl extends ServiceImpl<CommonUserMapper, CommonUser>
    implements CommonUserService {

}




