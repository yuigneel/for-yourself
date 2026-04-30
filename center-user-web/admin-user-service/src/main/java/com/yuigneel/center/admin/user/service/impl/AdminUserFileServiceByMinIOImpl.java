package com.yuigneel.center.admin.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuigneel.center.admin.user.mapper.AccountAvatarMapper;
import com.yuigneel.center.admin.user.model.domain.AccountAvatar;
import com.yuigneel.center.admin.user.service.AdminUserFileService;
import com.yuigneel.common.exception.ForYourselfException;
import com.yuigneel.common.model.enums.AccountIdentityTypeEnum;
import com.yuigneel.common.model.result.ResultCodeEnum;
import com.yuigneel.common.utils.MinioUtil;
import com.yuigneel.common.utils.UserContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserFileServiceByMinIOImpl extends ServiceImpl<AccountAvatarMapper, AccountAvatar>implements AdminUserFileService {
    private final MinioUtil minioUtil;
    private final TransactionTemplate transactionTemplate;
    private final AccountAvatarMapper accountAvatarMapper;

    /**
     * 上传用户头像到 MinIO
     * <p>
     * 该方法会执行以下操作：
     * 1. 验证上传文件是否为空
     * 2. 获取当前登录用户的UID
     * 3. 将头像文件上传至MinIO对象存储
     * 4. 保存或更新用户头像URL到数据库
     * 5. 如果数据库保存失败，会自动清理MinIO中已上传的文件（事务回滚）
     *
     * @param file 用户上传的头像文件
     * @throws ForYourselfException MinIO 上传失败或数据库保存失败时抛出异常
     */
    @Override
    public void uploadAvatar(MultipartFile file) {
        // ===文件名字校验
        if (file != null) {
            // 获取原始文件名
            String fileName = file.getOriginalFilename();
            // 文件名 为null / 空串 / 全空格 → 直接拦截
            if (!StringUtils.hasText(fileName)) {
                throw new ForYourselfException(ResultCodeEnum.FILENAME_FORMAT_ERROR, null);
            }
        }
        // ===获取数据库中的头像
        Long uid = UserContextUtil.getUid();
        LambdaQueryWrapper<AccountAvatar> eq = new LambdaQueryWrapper<AccountAvatar>().eq(AccountAvatar::getUid, uid).eq(AccountAvatar::getIdentityType, AccountIdentityTypeEnum.ADMIN);
        AccountAvatar one = this.getOne(eq);
        if (file == null || file.isEmpty()) {
            // ===如果前端传递头像为空，数据库头像也为空，直接啥都不干
            if (one == null) return;
            // ===如果前端为空，数据库不为空，删除数据库和MinIO中已上传的文件
            transactionTemplate.executeWithoutResult(status -> {
                // ---删除数据 忽略逻辑删除（物理删除）
                accountAvatarMapper.deleteByUidAndIdentityTypeIgnoreLogic(one.getUid(), AccountIdentityTypeEnum.ADMIN.getCode());
                // ---获取文件名，删除文件
                String avatarUrl = one.getAvatarUrl();
                boolean b = minioUtil.deleteFile(avatarUrl);
                if (!b) throw new ForYourselfException(ResultCodeEnum.MINIO_SERVICE_ERROR, null);
            });
        }
        // ===如果前端不为空，直接上传或跟新头像
        if (file != null && !file.isEmpty()) {
            transactionTemplate.executeWithoutResult(status -> {
                String newFileName;
                // ---上传新的头像
                try {
                    newFileName = minioUtil.uploadFile(file);
                } catch (Exception e) {
                    log.error("上传头像失败", e);
                    throw new ForYourselfException(ResultCodeEnum.MINIO_SERVICE_ERROR, null);
                }
                
                // ---判断数据库中是否有头像记录，决定是新增还是更新
                if (one == null) {
                    // 数据库中没有头像记录，直接新增
                    AccountAvatar accountAvatar = new AccountAvatar();
                    accountAvatar.setUid(uid);
                    accountAvatar.setIdentityType(AccountIdentityTypeEnum.ADMIN);
                    accountAvatar.setAvatarUrl(newFileName);
                    this.save(accountAvatar);
                } else {
                    // 数据库中有头像记录，先更新再删除旧文件
                    LambdaUpdateWrapper<AccountAvatar> set = new LambdaUpdateWrapper<AccountAvatar>().eq(AccountAvatar::getUid, uid)
                            .eq(AccountAvatar::getIdentityType, AccountIdentityTypeEnum.ADMIN)
                            .set(AccountAvatar::getAvatarUrl, newFileName);
                    this.update(set);
                    
                    // ---删除旧的头像文件（如果存在）
                    String oldFileName = one.getAvatarUrl();
                    if (StringUtils.hasText(oldFileName)) {
                        boolean b = minioUtil.deleteFile(oldFileName);
                        if (!b) throw new ForYourselfException(ResultCodeEnum.MINIO_SERVICE_ERROR, null);
                    }
                }
            });
        }
    }

    /**
     * 获取当前用户的头像 URL
     * <p>
     * 该方法会执行以下操作：
     * 1. 根据当前登录用户的UID查询头像记录
     * 2. 生成MinIO预签名URL（有效期1天）
     * 3. 返回可用于直接访问头像的临时URL
     *
     * @return MinIO预签名URL，有效期为1天
     * @throws ForYourselfException 当用户未找到头像记录或 MinIO 服务异常时抛出异常
     */
    @Override
    public String getAvatar() {
        LambdaQueryWrapper<AccountAvatar> eq = new LambdaQueryWrapper<AccountAvatar>().eq(AccountAvatar::getUid, UserContextUtil.getUid())
                .eq(AccountAvatar::getIdentityType, AccountIdentityTypeEnum.ADMIN);
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
