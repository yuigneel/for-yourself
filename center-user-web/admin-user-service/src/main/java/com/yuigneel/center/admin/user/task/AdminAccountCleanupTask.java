package com.yuigneel.center.admin.user.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuigneel.center.admin.user.mapper.AdminUserMapper;
import com.yuigneel.center.admin.user.model.domain.AdminUser;
import com.yuigneel.common.model.enums.AccountStatusEnum;
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
 * 
 * 作用：通过在 Nacos 或 application.yml 中修改 task.schedule.enabled 的值，
 * 可以在不改动代码的情况下，随时开启或关闭整个定时任务功能。
 */
@ConditionalOnProperty(name = "your.task.schedule.enabled", havingValue = "true", matchIfMissing = true)
public class AdminAccountCleanupTask {

    private final AdminUserMapper adminUserMapper; // 注入管理员 Mapper，用于执行数据库查询和删除
    private final TransactionTemplate transactionTemplate; // 注入事务模板，用于手动控制事务边界

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

            // --- 步骤 C: 在事务中执行物理删除 ---
            transactionTemplate.executeWithoutResult(status -> {
                try {
                    // physicalDeleteByUids: 调用我们在 XML 中手写的 DELETE 语句，根据 UID 列表直接物理删除数据
                    int count = adminUserMapper.physicalDeleteByUids(idsToDelete);
                    log.info("成功物理删除 {} 个过期/强制销号的管理员账号", count);
                    totalCleaned.addAndGet(count); // 将本批删除的数量累加到总计数器
                } catch (Exception e) {
                    // 如果删除过程中出错（如数据库连接断开），记录错误日志并抛出异常
                    log.error("批量删除管理员账号失败，事务回滚。IDs: {}", idsToDelete, e);
                    throw e; // 必须重新抛出异常，Spring 才能感知到错误并执行事务回滚
                }
            });
        }

        // 5. 任务结束，打印总结日志
        log.info("========================================");
        log.info("管理员账号清理任务执行完毕，共清理: {} 个账号", totalCleaned.get());
        log.info("========================================");
    }
}
