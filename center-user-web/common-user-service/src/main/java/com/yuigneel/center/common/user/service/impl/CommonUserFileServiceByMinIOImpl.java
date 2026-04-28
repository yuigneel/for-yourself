package com.yuigneel.center.common.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuigneel.center.common.user.mapper.AccountAvatarMapper;
import com.yuigneel.center.common.user.model.domain.AccountAvatar;
import com.yuigneel.center.common.user.service.CommonUserFileService;
import com.yuigneel.common.exception.ForYourselfException;
import com.yuigneel.common.model.enums.AccountIdentityTypeEnum;
import com.yuigneel.common.model.result.ResultCodeEnum;
import com.yuigneel.common.utils.MinioUtil;
import com.yuigneel.common.utils.UserContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CommonUserFileServiceByMinIOImpl extends ServiceImpl<AccountAvatarMapper, AccountAvatar> implements CommonUserFileService {

    private final MinioUtil minioUtil;

    /**
     * 上传用户头像到MinIO
     * <p>
     * 该方法会执行以下操作：
     * 1. 验证上传文件是否为空
     * 2. 获取当前登录用户的UID
     * 3. 将头像文件上传至MinIO对象存储
     * 4. 保存或更新用户头像URL到数据库
     * 5. 如果数据库保存失败，会自动清理MinIO中已上传的文件（事务回滚）
     *
     * @param file 用户上传的头像文件，不能为空
     * @throws ForYourselfException 当文件为空、MinIO上传失败或数据库保存失败时抛出异常
     */
    @Override
    public void uploadAvatar(MultipartFile file) {
        // 判断文件是否为空
        if (file.isEmpty()) throw new ForYourselfException(ResultCodeEnum.PARAMETER_ERROR, null);
        // 获取用户 uid
        Long uid = UserContextUtil.getUid();
        // 将用户头像传入MinIO
        String avatarUrl;
        try {
            avatarUrl = minioUtil.uploadFile(file);
        } catch (Exception e) {
            log.error("头像上传文件失败", e);
            throw new ForYourselfException(ResultCodeEnum.MINIO_SERVICE_ERROR, null);
        }
        try {
            // 将数据保存
            AccountAvatar one = this.getOne(new LambdaQueryWrapper<AccountAvatar>().eq(AccountAvatar::getUid, uid));
            if (one == null) {
                one = new AccountAvatar();
                one.setUid(uid);
                one.setIdentityType(AccountIdentityTypeEnum.USER);
                one.setAvatarUrl(avatarUrl);
                this.save(one);
            } else
                this.update(new LambdaUpdateWrapper<AccountAvatar>().eq(AccountAvatar::getUid, uid).eq(AccountAvatar::getIdentityType, AccountIdentityTypeEnum.USER).set(AccountAvatar::getAvatarUrl, avatarUrl));
        } catch (Exception e) {
            log.error("头像保存失败", e);
            //  删除MinIO中的文件
            try {
                minioUtil.deleteFile(avatarUrl);
            } catch (Exception ex) {
                log.error("MinIO文件删除失败", ex);
                throw new ForYourselfException(ResultCodeEnum.MINIO_SERVICE_ERROR, null);
            }
            throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
        }
    }

    /**
     * 获取当前用户的头像URL
     * <p>
     * 该方法会执行以下操作：
     * 1. 根据当前登录用户的UID查询头像记录
     * 2. 生成MinIO预签名URL（有效期1天）
     * 3. 返回可用于直接访问头像的临时URL
     *
     * @return MinIO预签名URL，有效期为1天
     * @throws ForYourselfException 当用户未找到头像记录或MinIO服务异常时抛出异常
     */
    @Override
    public String getAvatar() {
        LambdaQueryWrapper<AccountAvatar> eq = new LambdaQueryWrapper<AccountAvatar>().eq(AccountAvatar::getUid, UserContextUtil.getUid())
                .eq(AccountAvatar::getIdentityType, AccountIdentityTypeEnum.USER);
        AccountAvatar one = this.getOne(eq);
        if (one == null) return null;
        String presignedUrl;
        try {
            presignedUrl = minioUtil.getPresignedUrl(one.getAvatarUrl(), 1, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("获取头像失败", e);
            throw new ForYourselfException(ResultCodeEnum.MINIO_SERVICE_ERROR, null);
        }
        return presignedUrl;
    }
}
