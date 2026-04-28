package com.yuigneel.center.common.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuigneel.center.common.user.model.domain.AccountAvatar;
import org.springframework.web.multipart.MultipartFile;

public interface CommonUserFileService extends IService<AccountAvatar> {

    void uploadAvatar(MultipartFile file);

    String getAvatar();
}
