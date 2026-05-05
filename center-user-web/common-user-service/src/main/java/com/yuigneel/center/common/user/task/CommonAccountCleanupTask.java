package com.yuigneel.center.common.user.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuigneel.center.common.user.mapper.AccountExceptionStatusTimeMapper;
import com.yuigneel.center.common.user.mapper.UserMapper;
import com.yuigneel.center.common.user.model.domain.CommonUser;
import com.yuigneel.center.common.user.service.CommonUserFileService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 【教学注释】普通用户账号清理定时任务
 * <p>
 * 职责：
 * 1. 清理已逻辑删除 (is_deleted=1) 超过 3 个月的账号。
 * 2. 清理被强制销号 (FORCE_LOGOUT) 超过 120 天的账号。
 * 3. 联动清理关联的头像表、异常状态时间表以及物理头像文件。
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
@ConditionalOnProperty(name = "task.schedule.enabled", havingValue = "true", matchIfMissing = true)
public class CommonAccountCleanupTask {

    private final UserMapper userMapper; // 注入用户 Mapper，用于执行数据库查询和删除
    private final TransactionTemplate transactionTemplate; // 注入事务模板，用于手动控制事务边界
    private final CommonUserFileService commonUserFileServiceByMinIOImpl; // 注入文件服务，用于删除文件
    private final AccountExceptionStatusTimeMapper exceptionStatusTimeMapper; // 注入异常状态时间 Mapper，用于删除异常状态时间

    /**
     * 【教学注释】每天凌晨 3:00 执行清理任务
     * Cron 表达式详解: "秒 分 时 日 月 周"
     * 0 0 3 * * ? -> 每天 3 点 0 分 0 秒触发
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredAccounts() {
        log.info("========================================");
        log.info("开始执行普通用户账号清理定时任务...");
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

        // 【新增】使用 Set 记录删除失败的 UID，避免重复查询和处理
        Set<Long> failedUids = new HashSet<>();

        // 4. 开启循环分批处理
        while (true) {
            // --- 步骤 A: 查询一批需要删除的 UID（使用自定义 SQL，忽略 MP 的逻辑删除拦截） ---
            // 【修改】如果存在失败的 UID，则使用新的查询方法排除它们
            List<Long> idsToDelete;
            if (failedUids.isEmpty()) {
                idsToDelete = userMapper.selectUidsForCleanup(logicDeleteThreshold, forceLogoutThreshold, batchSize);
            } else {
                idsToDelete = userMapper.selectUidsForCleanupExcluding(
                    logicDeleteThreshold, forceLogoutThreshold, batchSize, new java.util.ArrayList<>(failedUids)
                );
            }

            // --- 步骤 B: 如果没有查到数据，说明清理完毕，跳出循环 ---
            if (idsToDelete.isEmpty()) {
                break;
            }

            // --- 步骤 C: 在事务中执行物理删除 ---
            for (Long uid : idsToDelete) {
                try {
                    transactionTemplate.executeWithoutResult(status -> {
                        // ~~~ 删除账户基础信息表
                        int mainCount = userMapper.physicalDeleteByUid(uid);
                        if (mainCount <= 0) {
                            log.error("主表删除失败，请检查数据库连接。UID：{}", uid);
                            throw new ForYourselfException(ResultCodeEnum.DATABASE_SERVICE_ERROR, "主表删除失败");
                        }
                        log.info("主表删除成功，UID：{}", uid);

                        // ~~~ 删除账号异常状态时间表 (直接删，不用先查)
                        int statusCount = exceptionStatusTimeMapper.physicalDeleteByUidAndIdentity(uid, AccountIdentityTypeEnum.USER);
                        if (statusCount <= 0) {
                            log.info("没有关联的账号异常状态时间表。UID：{}", uid);
                        } else {
                            log.info("账号异常状态时间表删除成功，UID：{}", uid);
                        }

                        // ~~~ 删除账号头像表记录及物理文件 (封装在 Service 中)
                        int count;
                        try {
                            count = commonUserFileServiceByMinIOImpl.physicalDeleteAvatarByUidAllowNull(uid);
                        } catch (Exception e) {
                            log.error("账号头像表删除失败，请检查数据库连接。UID：{}", uid);
                            throw new RuntimeException(e);
                        }
                        if (count <= 0) log.info("没有关联的账号头像表。UID：{}", uid);
                        else log.info("账号头像表删除成功，UID：{}", uid);
                    });
                    totalCleaned.incrementAndGet();
                    log.info("处理普通用户账号 {} 成功，已处理: {} 个账号", uid, totalCleaned.get());
                } catch (Exception e) {
                    // 【修改】记录单个账号处理失败的日志，并将 UID 加入失败集合
                    log.error("处理普通用户账号 {} 时发生错误，已跳过并加入失败列表", uid, e);
                    failedUids.add(uid);
                }
            }
        }
        // 5. 任务结束，打印总结日志
        log.info("========================================");
        log.info("普通用户账号清理任务执行完毕，共清理: {} 个账号", totalCleaned.get());
        if (!failedUids.isEmpty()) {
            log.warn("以下 {} 个账号因对象存储服务异常等原因删除失败，已被跳过: {}", failedUids.size(), failedUids);
        }
        log.info("========================================");
    }
}
