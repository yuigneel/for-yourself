package com.yuigneel.center.admin.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuigneel.center.admin.user.mapper.AccountAvatarMapper;
import com.yuigneel.center.admin.user.model.domain.AccountAvatar;
import com.yuigneel.center.admin.user.service.AdminUserFileService;
import com.yuigneel.common.exception.ForYourselfException;
import com.yuigneel.common.model.result.ResultCodeEnum;
import org.springframework.web.multipart.MultipartFile;

public class AdminUserFileServiceByCloudImpl extends ServiceImpl<AccountAvatarMapper, AccountAvatar> implements AdminUserFileService {

    @Override
    public void uploadAvatar(MultipartFile file) {
        //TODO
        throw new ForYourselfException(ResultCodeEnum.TODO, null);
    }

    @Override
    public String getAvatar() {
        //TODO
        throw new ForYourselfException(ResultCodeEnum.TODO, null);
    }

    @Override
    public void physicalDeleteAvatarByUid(Long uid) {
        //TODO
        throw new ForYourselfException(ResultCodeEnum.TODO, null);
    }

}
