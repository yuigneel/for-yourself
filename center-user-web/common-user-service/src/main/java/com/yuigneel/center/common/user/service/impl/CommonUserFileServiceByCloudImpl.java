package com.yuigneel.center.common.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuigneel.center.common.user.mapper.AccountAvatarMapper;
import com.yuigneel.center.common.user.model.domain.AccountAvatar;
import com.yuigneel.center.common.user.service.CommonUserFileService;
import com.yuigneel.common.exception.ForYourselfException;
import com.yuigneel.common.model.result.ResultCodeEnum;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CommonUserFileServiceByCloudImpl  extends ServiceImpl<AccountAvatarMapper, AccountAvatar> implements CommonUserFileService {
    @Override
    public void uploadAvatar(MultipartFile file) {
        //TODO
        throw new ForYourselfException(ResultCodeEnum.TODO,null);
    }

    @Override
    public String getAvatar() {
      //TODO
        throw new ForYourselfException(ResultCodeEnum.TODO,null);
    }
}
