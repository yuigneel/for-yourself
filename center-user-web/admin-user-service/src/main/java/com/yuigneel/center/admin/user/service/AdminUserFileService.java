package com.yuigneel.center.admin.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuigneel.center.admin.user.model.domain.AccountAvatar;
import org.springframework.web.multipart.MultipartFile;

public interface AdminUserFileService extends IService<AccountAvatar> {

    void uploadAvatar(MultipartFile file);

    String getAvatar();

    void physicalDeleteAvatarByUid(Long uid);
}
