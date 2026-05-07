/*
 * For Yourself - A graduation project by the author, serving as a demonstration for the future complete project ecosystem
 * Copyright (C) 2026  Yu·Igneel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

/**
 * 普通用户文件服务实现类（基于MinIO）
 * <p>
 * 实现CommonUserFileService接口，提供基于MinIO对象存储的文件管理功能
 * </p>
 *
 * @author yuigneel
 * @since 2026-05-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommonUserFileServiceByMinIOImpl extends ServiceImpl<AccountAvatarMapper, AccountAvatar> implements CommonUserFileService {

    private final MinioUtil minioUtil;
    private final TransactionTemplate transactionTemplate;
    private final AccountAvatarMapper accountAvatarMapper;

    /**
     * 上传用户头像
     * <p>
     * 逻辑流程：
     * 1. 校验文件名格式
     * 2. 查询数据库中是否已有头像记录
     * 3. 如果前端未传文件且数据库无记录，直接返回
     * 4. 如果前端未传文件但数据库有记录，物理删除数据库记录和MinIO文件
     * 5. 如果前端传了文件，上传到MinIO并更新数据库（新增或更新）
     * 6. 如果是更新操作，删除旧的MinIO文件
     * </p>
     * <p>
     * 特殊事项：
     * - 使用事务保证数据库和MinIO操作的一致性
     * - 更新时会先保存新文件再删除旧文件，避免文件丢失
     * </p>
     *
     * @param file 用户上传的头像文件，可为null表示删除头像
     * @author yuigneel
     * @since 2026-05-06
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
        LambdaQueryWrapper<AccountAvatar> eq = new LambdaQueryWrapper<AccountAvatar>().eq(AccountAvatar::getUid, uid).eq(AccountAvatar::getIdentityType, AccountIdentityTypeEnum.USER);
        AccountAvatar one = this.getOne(eq);
        if (file == null || file.isEmpty()) {
            // ===如果前端传递头像为空，数据库头像也为空，直接啥都不干
            if (one == null) return;
            // ===如果前端为空，数据库不为空，删除数据库和MinIO中已上传的文件
            transactionTemplate.executeWithoutResult(status -> {
                // ---删除数据 忽略逻辑删除（物理删除）
                accountAvatarMapper.deleteByUidAndIdentityTypeIgnoreLogic(one.getUid(), AccountIdentityTypeEnum.USER.getCode());
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
                    accountAvatar.setIdentityType(AccountIdentityTypeEnum.USER);
                    accountAvatar.setAvatarUrl(newFileName);
                    this.save(accountAvatar);
                } else {
                    // 数据库中有头像记录，先更新再删除旧文件
                    LambdaUpdateWrapper<AccountAvatar> set = new LambdaUpdateWrapper<AccountAvatar>().eq(AccountAvatar::getUid, uid)
                            .eq(AccountAvatar::getIdentityType, AccountIdentityTypeEnum.USER)
                            .set(AccountAvatar::getAvatarUrl, newFileName);
                    this.update(set);

                    // ---删除旧的头像文件
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
     * 获取当前用户的头像URL
     * <p>
     * 逻辑流程：
     * 1. 根据当前登录用户UID查询头像记录
     * 2. 如果不存在返回null
     * 3. 生成MinIO预签名URL（有效期1天）
     * </p>
     *
     * @return MinIO预签名URL，有效期为1天；如果用户没有头像则返回null
     * @author yuigneel
     * @since 2026-05-06
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

    /**
     * 物理删除指定UID的用户头像
     * <p>
     * 逻辑流程：
     * 1. 查询头像记录，不存在则抛异常
     * 2. 在事务中物理删除数据库记录
     * 3. 删除MinIO中的文件
     * </p>
     * <p>
     * 特殊事项：
     * - 如果头像记录不存在会抛出异常
     * - 使用事务保证数据一致性
     * </p>
     *
     * @param uid 用户ID
     * @author yuigneel
     * @since 2026-05-06
     */
    @Override
    public void physicalDeleteAvatarByUid(Long uid) {
        // ===先获得账号头像对象
        LambdaQueryWrapper<AccountAvatar> eq = new LambdaQueryWrapper<AccountAvatar>().eq(AccountAvatar::getUid, uid).eq(AccountAvatar::getIdentityType, AccountIdentityTypeEnum.USER);
        AccountAvatar one = this.getOne(eq);
        if (one == null) throw new ForYourselfException(ResultCodeEnum.ACCOUNT_NOT_FOUND_OR_CANCELLED, null);
        final String avatarUrl = one.getAvatarUrl();
        transactionTemplate.executeWithoutResult(status -> {
            // ===直接物理删除头像数据，返回删除行数
            int i = accountAvatarMapper.deleteByUidAndIdentityTypeIgnoreLogic(uid, AccountIdentityTypeEnum.USER.getCode());
            if (i <= 0) throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
            // ===删除文件
            boolean b = minioUtil.deleteFile(avatarUrl);
            if (!b) throw new ForYourselfException(ResultCodeEnum.MINIO_SERVICE_ERROR, null);
        });
    }

    /**
     * 物理删除指定UID的用户头像（允许为空）
     * <p>
     * 逻辑流程：
     * 1. 查询头像记录，不存在则返回0
     * 2. 在事务中物理删除数据库记录
     * 3. 删除MinIO中的文件
     * 4. 返回删除结果（1表示成功，0表示未找到）
     * </p>
     * <p>
     * 特殊事项：
     * - 与physicalDeleteAvatarByUid的区别是：头像不存在时不抛异常，返回0
     * - 适用于注销等场景，允许用户没有头像
     * </p>
     *
     * @param uid 用户ID
     * @return 删除的记录数，1表示成功删除，0表示未找到记录
     * @author yuigneel
     * @since 2026-05-06
     */
    @Override
    public int physicalDeleteAvatarByUidAllowNull(Long uid) {
        // ===先获得账号头像对象
        LambdaQueryWrapper<AccountAvatar> eq = new LambdaQueryWrapper<AccountAvatar>().eq(AccountAvatar::getUid, uid).eq(AccountAvatar::getIdentityType, AccountIdentityTypeEnum.USER);
        AccountAvatar one = this.getOne(eq);
        if (one == null) return 0;
        final String avatarUrl = one.getAvatarUrl();
        Boolean execute = transactionTemplate.execute(status -> {
            // ===直接物理删除头像数据，返回删除行数
            int i = accountAvatarMapper.deleteByUidAndIdentityTypeIgnoreLogic(uid, AccountIdentityTypeEnum.USER.getCode());
            if (i <= 0) throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, null);
            // ===删除文件
            boolean b = minioUtil.deleteFile(avatarUrl);
            if (!b) throw new ForYourselfException(ResultCodeEnum.MINIO_SERVICE_ERROR, null);
            return true;
        });
        return Boolean.TRUE.equals(execute) ? 1 : 0;
    }

}
