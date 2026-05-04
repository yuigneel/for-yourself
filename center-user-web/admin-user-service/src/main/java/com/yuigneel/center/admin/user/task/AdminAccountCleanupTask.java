package com.yuigneel.center.admin.user.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuigneel.center.admin.user.mapper.AccountExceptionStatusTimeMapper;
import com.yuigneel.center.admin.user.mapper.AdminUserMapper;
import com.yuigneel.center.admin.user.model.domain.AccountAvatar;
import com.yuigneel.center.admin.user.model.domain.AdminUser;
import com.yuigneel.center.admin.user.service.AdminUserFileService;
import com.yuigneel.common.exception.ForYourselfException;
import com.yuigneel.common.model.enums.AccountIdentityTypeEnum;
import com.yuigneel.common.model.enums.AccountStatusEnum;
import com.yuigneel.common.model.result.ResultCodeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 【教学注释】管理员账号清理定时任务
 * <p>
 * 职责：
 * 1. 清理已逻辑删除 (is_deleted=1) 超过 3 个月的管理员账号。
 * 2. 清理被强制销号 (FORCE_LOGOUT) 超过 120 天的管理员账号。
 */
@Slf4j
@Component
@RequiredArgsConstructor // Lombok 注解：自动生成包含 final 字段的构造器，实现依赖注入

/**
 * 【教学注释】定时任务总开关
 * name: 配置文件中属性的名字（例如 task.schedule.enabled）
 * havingValue: 只有当该属性的值等于 "true" 时，这个类才会被加载
 * matchIfMissing: 如果配置文件里没写这个属性，默认是否加载？(true=默认开启)
 * 作用：通过在 Nacos 或 application.yml 中修改 task.schedule.enabled 的值，
 * 可以在不改动代码的情况下，随时开启或关闭整个定时任务功能。
 */
@ConditionalOnProperty(name = "your.task.schedule.enabled", havingValue = "true", matchIfMissing = true)
public class AdminAccountCleanupTask {

    private final AdminUserMapper adminUserMapper; // 注入管理员 Mapper，用于执行数据库查询和删除
    private final TransactionTemplate transactionTemplate; // 注入事务模板，用于手动控制事务边界
    private final AdminUserFileService adminUserFileServiceByMinIOImpl; // 注入文件服务，用于删除文件
    private final AccountExceptionStatusTimeMapper exceptionStatusTimeMapper; // 注入异常状态时间 Mapper，用于删除异常状态时间

    /**
     * 【教学注释】每天凌晨 3:00 执行清理任务
     * Cron 表达式详解: "秒 分 时 日 月 周"
     * 0 0 3 * * ? -> 每天 3 点 0 分 0 秒触发
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredAccounts() {
        log.info("========================================");
        log.info("开始执行管理员账号清理定时任务...");
        log.info("========================================");

        // 1. 获取当前时间，作为计算过期阈值的基准
        LocalDateTime now = LocalDateTime.now();

        // 2. 计算两个不同的时间阈值（截止时间点）
        // minusMonths(3): 向前推 3 个月。如果 update_time 早于这个时间点，说明已经“死”了 3 个月了
        LocalDateTime logicDeleteThreshold = now.minusMonths(3);
        // minusDays(120): 向前推 120 天。强制销号的账号保留期更长
        LocalDateTime forceLogoutThreshold = now.minusDays(120);

        // 3. 使用原子计数器统计清理总数（Lambda 表达式中不能使用普通局部变量进行累加）
        AtomicInteger totalCleaned = new AtomicInteger(0);
        int batchSize = 500; // 定义每批处理的数量，防止一次性删除太多导致数据库锁表或内存溢出

        // 4. 开启循环分批处理
        while (true) {
            // --- 步骤 A: 查询一批需要删除的 UID ---
            List<Long> idsToDelete = adminUserMapper.selectObjs(
                            // 创建 Lambda 查询包装器，提供类型安全的链式调用
                            new LambdaQueryWrapper<AdminUser>()
                                    // select(AdminUser::getUid): 只查询 uid 字段，不查其他字段，减少网络传输和内存占用
                                    .select(AdminUser::getUid)
                                    // and(wrapper -> ...): 将内部的多个条件包裹在一个括号内，确保逻辑优先级
                                    .and(wrapper -> wrapper
                                            // apply(...): 手写 SQL 片段。查询逻辑删除且更新时间早于 3 个月前的记录
                                            .apply("is_deleted = 1 AND update_time < {0}", logicDeleteThreshold)
                                            // or(w -> ...): 或者满足以下条件（逻辑删除 OR 强制销号）
                                            .or(w -> w
                                                    // eq(...): 等于条件。查询账户状态为“强制销号”的记录
                                                    .eq(AdminUser::getAccountStatus, AccountStatusEnum.ACCOUNT_STATUS_FORCE_LOGOUT)
                                                    // apply(...): 且更新时间早于 120 天前
                                                    .apply("update_time < {0}", forceLogoutThreshold)
                                            )
                                    )
                                    // last("LIMIT ..."): 在 SQL 末尾强行拼接 LIMIT 语句，实现分批查询
                                    .last("LIMIT " + batchSize)
                    )
                    // stream().map(...).toList(): 将查询结果从 Object 列表转换为 Long (UID) 列表
                    .stream().map(obj -> (Long) obj).toList();

            // --- 步骤 B: 如果没有查到数据，说明清理完毕，跳出循环 ---
            if (idsToDelete.isEmpty()) {
                break;
            }

            // --- 步骤 C: 在事务中执行物理删除，并在事务外删除文件 ---
            for (Long uid : idsToDelete) {
                try {
                    transactionTemplate.executeWithoutResult(status -> {
                        // ~~~ 删除账户基础信息表
                        int mainCount = adminUserMapper.physicalDeleteByUid(uid);
                        if (mainCount <= 0) {
                            log.error("主表删除失败，请检查数据库连接。UID：{}", uid);
                            throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, "主表删除失败");
                        }
                        log.info("主表删除成功，UID：{}", uid);

                        // ~~~ 删除账号异常状态时间表 (直接删，不用先查)
                        int yuigneel = exceptionStatusTimeMapper.physicalDeleteByUidAndIdentity(uid, AccountIdentityTypeEnum.ADMIN);
                        if (yuigneel <= 0) {
                            log.info("没有关联的账号异常状态时间表。UID：{}", uid);
                        } else {
                            log.info("账号异常状态时间表删除成功，UID：{}", uid);
                        }

                        // ~~~ 删除账号头像表记录 （由于调用的方法要求必须有这个对象，所以必须要查）
                        LambdaQueryWrapper<AccountAvatar> eq = new LambdaQueryWrapper<AccountAvatar>().eq(AccountAvatar::getUid, uid).eq(AccountAvatar::getIdentityType, AccountIdentityTypeEnum.ADMIN);
                        AccountAvatar avatar = adminUserFileServiceByMinIOImpl.getOne(eq);
                        if (avatar == null) {
                            log.info("没有关联的账号头像表。UID：{}", uid);
                        } else {
                            try {
                                adminUserFileServiceByMinIOImpl.physicalDeleteAvatarByUid(uid);
                            } catch (Exception e) {
                                log.error("删除账号头像表记录时发生错误。UID:{}", uid, e);
                                throw e;
                            }
                        }
                        log.info("整个管理员账号删除成功，UID：{}", uid);
                    });
                    totalCleaned.incrementAndGet();
                } catch (Exception e) {
                    // 记录单个账号处理失败的日志，但不中断整个批次
                    log.error("处理管理员账号 {} 时发生错误，已跳过", uid, e);
                }
            }
        }
        // 5. 任务结束，打印总结日志
        log.info("========================================");
        log.info("管理员账号清理任务执行完毕，共清理: {} 个账号", totalCleaned.get());
        log.info("========================================");
    }
}
