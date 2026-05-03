package com.yuigneel.center.admin.user.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yuigneel.center.admin.user.mapper.AccountExceptionStatusTimeMapper;
import com.yuigneel.center.admin.user.mapper.AdminUserMapper;
import com.yuigneel.center.admin.user.model.domain.AccountExceptionStatusTime;
import com.yuigneel.center.admin.user.model.domain.AdminUser;
import com.yuigneel.common.events.UserStatusCheckEvent;
import com.yuigneel.common.exception.ForYourselfException;
import com.yuigneel.common.model.enums.AccountIdentityTypeEnum;
import com.yuigneel.common.model.enums.AccountStatusEnum;
import com.yuigneel.common.model.result.ResultCodeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

/**
 * 【教学注释】管理员账号状态监听器
 * <p>
 * 1. 职责：监听拦截器发布的校验事件，专门处理“管理员”身份的状态查询。
 * 2. 自动解封：如果发现封禁已过期，会自动清理异常记录并恢复管理员主表状态。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminStatusCheckListener {

    private final AccountExceptionStatusTimeMapper exceptionStatusTimeMapper;
    private final AdminUserMapper adminUserMapper; // 注入管理员 Mapper 用于同步主表状态
    private final TransactionTemplate transactionTemplate;

    /**
     * 【教学注释】监听管理员状态校验事件
     *
     * @param event 拦截器发布的事件对象
     */
    @EventListener
    public void handleAdminStatusCheck(UserStatusCheckEvent event) {
        // 1. 身份过滤：只处理管理员的事件
        if (event.getIdentityType() != AccountIdentityTypeEnum.ADMIN) {
            return; //鲁棒性考虑
        }

        log.debug("监听器收到管理员 {} 的状态校验请求", event.getUid());

        // 1. 先查询主表获取当前账号状态
        AdminUser admin = adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>()
                        .eq(AdminUser::getUid, event.getUid())
                        .select(AdminUser::getAccountStatus)
        );

        if (admin == null) {
            throw new ForYourselfException(ResultCodeEnum.ACCOUNT_NOT_FOUND_OR_CANCELLED, null);
        }

        AccountStatusEnum status = admin.getAccountStatus();

        // 2. 正常状态直接放行
        if (status == AccountStatusEnum.ACCOUNT_STATUS_NORMAL) {
            event.setBanned(false, null);
            return;
        }

        // 3. 强制销号直接拦截（不查时间表）
        if (status == AccountStatusEnum.ACCOUNT_STATUS_FORCE_LOGOUT) {
            log.warn("管理员 {} 已被强制销号，拦截请求", event.getUid());
            event.setBanned(true, null);
            return;
        }

        // 4. 警告或封禁：查询异常时间表判断是否过期
        if (status == AccountStatusEnum.ACCOUNT_STATUS_WARNING || 
            status == AccountStatusEnum.ACCOUNT_STATUS_LOCKED) {
            
            AccountExceptionStatusTime record = exceptionStatusTimeMapper.selectOne(
                    new LambdaQueryWrapper<AccountExceptionStatusTime>()
                            .eq(AccountExceptionStatusTime::getUid, event.getUid())
                            .eq(AccountExceptionStatusTime::getIdentityType, AccountIdentityTypeEnum.ADMIN)
            );

            if (record != null && record.getExpireTime().isAfter(LocalDateTime.now())) {
                // 未过期，拦截
                log.warn("管理员 {} 处于{}状态，到期时间: {}", event.getUid(), status.getName(), record.getExpireTime());
                event.setBanned(true, record.getExpireTime());
            } else {
                // 已过期或无记录，执行自动恢复
                log.info("管理员 {} 的{}状态已过期，执行自动恢复", event.getUid(), status.getName());
                autoUnbanAdmin(event.getUid());
                event.setBanned(false, null);
            }
        }
    }

    /**
     * 【教学注释】管理员自动解封逻辑
     * 1. 物理删除异常状态时间表中的记录。
     * 2. 将管理员主表的状态改回“正常”。
     */
    private void autoUnbanAdmin(Long uid) {
        log.info("开始执行管理员 {} 的自动解封逻辑", uid);
        transactionTemplate.executeWithoutResult(status -> {
            try {
                // 1. 物理删除异常记录
                int deleteCount = exceptionStatusTimeMapper.physicalDeleteByUidAndIdentity(uid, AccountIdentityTypeEnum.ADMIN);
                if (deleteCount <= 0) {
                    log.warn("管理员 {} 自动解封时未找到异常记录，可能已被手动删除", uid);
                }

                // 2. 同步更新主表状态为正常
                LambdaUpdateWrapper<AdminUser> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(AdminUser::getUid, uid)
                        .set(AdminUser::getAccountStatus, AccountStatusEnum.ACCOUNT_STATUS_NORMAL)
                        .set(AdminUser::getUpdateBy, null);  // 清空更新人
                int updateCount = adminUserMapper.update(null, updateWrapper);
                if (updateCount <= 0) {
                    throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, "自动解封失败");
                }
                log.info("管理员 {} 自动解封成功，事务提交", uid);
            } catch (Exception e) {
                log.error("管理员 {} 自动解封失败，事务回滚。原因: {}", uid, e.getMessage());
                throw e; // 必须重新抛出以触发事务回滚
            }
        });
    }
}
