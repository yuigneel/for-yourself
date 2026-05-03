package com.yuigneel.center.common.user.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuigneel.center.common.user.mapper.AccountExceptionStatusTimeMapper;
import com.yuigneel.center.common.user.mapper.UserMapper;
import com.yuigneel.center.common.user.model.domain.AccountExceptionStatusTime;
import com.yuigneel.center.common.user.model.domain.CommonUser;
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
 * 【教学注释】普通用户账号状态监听器
 * <p>
 * 1. 为什么要加 @Component？
 * - 只有被 Spring 容器管理的 Bean，其内部的 @EventListener 才会被识别和注册。
 * <p>
 * 2. 为什么这个方法没有返回值？
 * - 因为我们采用的是“结果回填”模式。监听器直接修改传入的 event 对象，
 * 拦截器那边就能拿到最新的结果，不需要通过返回值传递。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatusCheckListener {

    private final AccountExceptionStatusTimeMapper exceptionStatusTimeMapper; // 注入异常状态时间表 Mapper
    private final UserMapper userMapper; // 注入用户 Mapper 用于同步主表状态
    private final TransactionTemplate transactionTemplate;

    /**
     * 【教学注释】监听用户状态校验事件
     *
     * @param event 拦截器发布的事件对象
     */
    @EventListener
    public void handleUserStatusCheck(UserStatusCheckEvent event) {
        // 1. 身份过滤：只处理普通用户的事件
        if (event.getIdentityType() != AccountIdentityTypeEnum.USER) {
            return; //鲁棒性考虑
        }

        log.debug("监听器收到普通用户 {} 的状态校验请求", event.getUid());

        // 1. 先查询主表获取当前账号状态
        CommonUser user = userMapper.selectOne(
                new LambdaQueryWrapper<CommonUser>()
                        .eq(CommonUser::getUid, event.getUid())
                        .select(CommonUser::getAccountStatus) // 只查状态字段，提高性能
        );

        if (user == null) {
            throw new ForYourselfException(ResultCodeEnum.ACCOUNT_NOT_FOUND_OR_CANCELLED, null);// 用户不存在由其他逻辑处理，这里默认放行或根据业务定

        }

        AccountStatusEnum status = user.getAccountStatus();

        // 2. 正常状态直接放行
        if (status == AccountStatusEnum.ACCOUNT_STATUS_NORMAL) {
            event.setBanned(false, null);
            return;
        }

        // 3. 强制销号直接拦截（不查时间表）
        if (status == AccountStatusEnum.ACCOUNT_STATUS_FORCE_LOGOUT) {
            log.warn("普通用户 {} 已被强制销号，拦截请求", event.getUid());
            event.setBanned(true, null);
            return;
        }

        // 4. 警告或封禁：查询异常时间表判断是否过期
        if (status == AccountStatusEnum.ACCOUNT_STATUS_WARNING ||
                status == AccountStatusEnum.ACCOUNT_STATUS_LOCKED) {

            AccountExceptionStatusTime record = exceptionStatusTimeMapper.selectOne(
                    new LambdaQueryWrapper<AccountExceptionStatusTime>()
                            .eq(AccountExceptionStatusTime::getUid, event.getUid())
                            .eq(AccountExceptionStatusTime::getIdentityType, AccountIdentityTypeEnum.USER)
            );

            if (record != null && record.getExpireTime().isAfter(LocalDateTime.now())) {
                // 未过期，拦截
                log.warn("普通用户 {} 处于{}状态，到期时间: {}", event.getUid(), status.getName(), record.getExpireTime());
                event.setBanned(true, record.getExpireTime());
            } else {
                // 已过期或无记录，执行自动恢复
                log.info("普通用户 {} 的{}状态已过期，执行自动恢复", event.getUid(), status.getName());
                autoUnbanUser(event.getUid());
                event.setBanned(false, null);
            }
        }
    }

    /**
     * 【教学注释】自动解封逻辑
     * 1. 物理删除异常状态时间表中的记录。
     * 2. 将用户主表的状态改回“正常”。
     */
    private void autoUnbanUser(Long uid) {
        log.info("开始执行普通用户 {} 的自动解封逻辑", uid);
        transactionTemplate.executeWithoutResult(status -> {
            try {
                // 1. 物理删除异常记录
                int deleteCount = exceptionStatusTimeMapper.physicalDeleteByUidAndIdentity(uid, AccountIdentityTypeEnum.USER);
                if (deleteCount <= 0) {
                    log.warn("普通用户 {} 自动解封时未找到异常记录，可能已被手动删除", uid);
                }
                // 2. 同步更新主表状态为正常
                com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<CommonUser> updateWrapper =
                        new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
                updateWrapper.eq(CommonUser::getUid, uid)
                        .set(CommonUser::getAccountStatus, AccountStatusEnum.ACCOUNT_STATUS_NORMAL)
                        .set(CommonUser::getUpdateBy, null);    // 清空更新人
                int updateCount = userMapper.update(null, updateWrapper);
                if (updateCount <= 0) {
                    throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, "自动解封失败");
                }
                log.info("普通用户 {} 自动解封成功，事务提交", uid);
            } catch (Exception e) {
                log.error("普通用户 {} 自动解封失败，事务回滚。原因: {}", uid, e.getMessage());
                throw e; // 必须重新抛出以触发事务回滚
            }
        });
    }
}
