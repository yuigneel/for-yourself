-- ==============================
-- 生成数据库：用户中心库（规范命名）
CREATE DATABASE IF NOT EXISTS `db_user_center`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
-- 切换数据库
USE `db_user_center`;
-- ==============================
-- 普通用户基础信息表（加t_前缀，规范命名）
DROP TABLE IF EXISTS `t_common_user`;
CREATE TABLE `t_common_user` (
-- 说明：UNSIGNED表示无符号整数，只能存储非负数，可扩大正数取值范围
                                 `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键自增ID',
                                 `uid` BIGINT NOT NULL COMMENT '用户唯一业务UID',
                                 `email` VARCHAR(128) NOT NULL COMMENT '登录邮箱(唯一)',
                                 `nickname` VARCHAR(64) NOT NULL COMMENT '用户昵称(唯一)',
                                 `password` VARCHAR(128) NOT NULL COMMENT 'BCrypt加密后的密码',
                                 `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别：0-未知 3-强男 2-男 1-弱男 -1-弱女 -2-女 -3-强女',
                                 `birthday` DATE DEFAULT NULL COMMENT '出生日期',
    -- 🔥通用字段，不绑定业务
                                 `join_date` DATE NOT NULL COMMENT '平台入驻日期',
                                 `account_status` TINYINT NOT NULL DEFAULT 0 COMMENT '账户状态：3-强制销号 2-禁用 1-警告 0-正常',
                                 `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
                                 `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间(自动生成)',
                                 `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间(自动更新)',
                                 `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '最后更新人UID',

    -- 索引
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_uid` (`uid`),
                                 UNIQUE KEY `uk_email` (`email`),
                                 UNIQUE KEY `uk_nickname` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='普通用户基础信息表';
-- 管理员用户基础信息表（加t_前缀，规范命名）
DROP TABLE IF EXISTS `t_admin_user`;
CREATE TABLE `t_admin_user` (
                                `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键自增ID',
                                `uid` BIGINT NOT NULL COMMENT '管理员唯一业务UID',
                                `email` VARCHAR(128) NOT NULL COMMENT '登录邮箱(唯一)',
                                `nickname` VARCHAR(64) NOT NULL COMMENT '管理员昵称(唯一)',
                                `password` VARCHAR(128) NOT NULL COMMENT 'BCrypt加密后的密码',
                                `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别：0-未知 3-强男 2-男 1-弱男 -1-弱女 -2-女 -3-强女',
                                `birthday` DATE DEFAULT NULL COMMENT '出生日期',
                                `join_date` DATE NOT NULL COMMENT '平台入驻日期',
                                `account_permission` TINYINT NOT NULL DEFAULT 3 COMMENT '账户权限：0-最高权限 数值越大权限越低 当前最低为3',
                                `account_status` TINYINT NOT NULL DEFAULT 0 COMMENT '账户状态：3-强制销号 2-禁用 1-警告 0-正常',
                                `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
                                `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间(自动生成)',
                                `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间(自动更新)',
                                `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '最后更新人UID',

    -- 索引
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_uid` (`uid`),
                                UNIQUE KEY `uk_email` (`email`),
                                UNIQUE KEY `uk_nickname` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员用户基础信息表';

-- 插入初始管理员账号（最高权限）
INSERT INTO `t_admin_user` (`uid`, `email`, `nickname`, `password`, `join_date`, `account_permission`)
VALUES
(2043953017778077696, 'yu_lgnier@outlook.com', '小白', '$2a$10$FwxZr3z10XgKu24nfJPkheB3W4LGQszgKxhCInaZ9akRJ0n5isZkK', CURDATE(), 0),
(2045827709510090752, 'yuigneel@outlook.com', '小黑', '$2a$10$pY9kwEJJB99zylONtPXgyeo8JFVjyOYk6LlZCkAmE2HpiRhNvFmQ6', CURDATE(), 0),
(2049676213701574656, 'yulgnier@gmail.com', '小蓝', '$2a$10$pY9kwEJJB99zylONtPXgyeo8JFVjyOYk6LlZCkAmE2HpiRhNvFmQ6', CURDATE(), 0);


-- ==============================
-- 账号头像表（统一管理用户和管理员头像）
DROP TABLE IF EXISTS `t_account_avatar`;
CREATE TABLE `t_account_avatar` (
                                    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键自增ID',
                                    `identity_type` TINYINT NOT NULL COMMENT '身份类型：0-管理员 1-普通用户',
                                    `uid` BIGINT NOT NULL COMMENT '用户/管理员唯一业务UID',
                                    `avatar_url` VARCHAR(512) NOT NULL COMMENT '头像图片唯一地址',
                                    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
                                    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间(自动生成)',
                                    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间(自动更新)',

                                    -- 索引
                                    PRIMARY KEY (`id`),
                                    KEY `idx_identity_uid` (`identity_type`, `uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账号头像表';


-- ============================================
-- 账号异常状态时间表
-- 作者：yuigneel
-- 日期：2026-05-02
-- 用途：记录管理员和用户的封禁、警告等异常状态的到期时间
-- 说明：一个账号同一时间只能有一个异常状态（不叠加）
-- ============================================
CREATE TABLE t_account_exception_status_time (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID（自增）',

    uid BIGINT NOT NULL COMMENT '账号UID（雪花ID）',

    identity_type TINYINT NOT NULL COMMENT '身份类型：0-管理员 1-普通用户（对应AccountIdentityTypeEnum枚举）',

    exception_type TINYINT DEFAULT NULL COMMENT '异常类型：1-警告 2-封禁 3-强制注销',

    expire_time DATETIME DEFAULT NULL COMMENT '异常状态到期时间（精确到秒）',

    reason VARCHAR(500) DEFAULT NULL COMMENT '异常原因/封禁理由',

    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除（默认0）',

    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    update_by BIGINT DEFAULT NULL COMMENT '更新人UID（操作的管理员UID，NULL表示系统自动更新）',


    -- 联合唯一索引：一个账号的同一身份只能有一条异常状态记录（防止状态叠加）
    UNIQUE KEY uk_uid_identity (uid, identity_type),

    -- 查询优化索引：快速查找已过期的记录
    INDEX idx_expire_time (expire_time)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账号异常状态时间表';


-- ============================================
-- 测试数据：用于验证定时任务清理逻辑（全排列组合）
-- 当前模拟时间：2026-05-05
-- 逻辑删除清理阈值：update_time < 2026-02-05
-- 强制销号清理阈值：update_time < 2026-01-05
-- ============================================

-- 1. 普通用户测试数据 (t_common_user)
INSERT INTO `t_common_user` (`uid`, `email`, `nickname`, `password`, `gender`, `join_date`, `account_status`, `is_deleted`, `update_time`) VALUES
-- [应清理] 因子：逻辑删除(1) + 状态非3(0) + 时间过期(2025-12-01)
(1000000000000000001, 'test_u1@user.com', 'U-逻删-过期-待清', '$2a$10$pY9kwEJJB99zylONtPXgyeo8JFVjyOYk6LlZCkAmE2HpiRhNvFmQ6', 0, '2025-01-01', 0, 1, '2025-12-01 10:00:00'),

-- [应清理] 因子：逻辑删除(1) + 状态强制(3) + 时间过期(2025-11-01)
(1000000000000000002, 'test_u2@user.com', 'U-强销-过期-待清', '$2a$10$pY9kwEJJB99zylONtPXgyeo8JFVjyOYk6LlZCkAmE2HpiRhNvFmQ6', 0, '2025-01-01', 3, 1, '2025-11-01 10:00:00'),

-- [应清理] 因子：未逻删(0) + 状态强制(3) + 时间过期(2025-10-01)
(1000000000000000003, 'test_u3@user.com', 'U-强销-未删-待清', '$2a$10$pY9kwEJJB99zylONtPXgyeo8JFVjyOYk6LlZCkAmE2HpiRhNvFmQ6', 0, '2025-01-01', 3, 0, '2025-10-01 10:00:00'),

-- [保留] 因子：逻辑删除(1) + 状态警告(1) + 时间未过(2026-04-01)
(1000000000000000004, 'test_u4@user.com', 'U-逻删-警告-保留', '$2a$10$pY9kwEJJB99zylONtPXgyeo8JFVjyOYk6LlZCkAmE2HpiRhNvFmQ6', 0, '2025-01-01', 1, 1, '2026-04-01 10:00:00'),

-- [保留] 因子：未逻删(0) + 状态正常(0) + 时间最新
(1000000000000000005, 'test_u5@user.com', 'U-正常-活跃-保留', '$2a$10$pY9kwEJJB99zylONtPXgyeo8JFVjyOYk6LlZCkAmE2HpiRhNvFmQ6', 2, '2025-01-01', 0, 0, '2026-05-01 10:00:00'),

-- [保留] 因子：未逻删(0) + 状态禁用(2) + 时间未过强销线
(1000000000000000006, 'test_u6@user.com', 'U-禁用-观察-保留', '$2a$10$pY9kwEJJB99zylONtPXgyeo8JFVjyOYk6LlZCkAmE2HpiRhNvFmQ6', 0, '2025-01-01', 2, 0, '2026-03-01 10:00:00');

-- 2. 管理员测试数据 (t_admin_user)
INSERT INTO `t_admin_user` (`uid`, `email`, `nickname`, `password`, `gender`, `join_date`, `account_permission`, `account_status`, `is_deleted`, `update_time`) VALUES
-- [应清理] 因子：逻辑删除(1) + 状态非3(0) + 时间过期
(2000000000000000001, 'test_a1@admin.com', 'A-逻删-过期-待清', '$2a$10$pY9kwEJJB99zylONtPXgyeo8JFVjyOYk6LlZCkAmE2HpiRhNvFmQ6', 0, '2025-01-01', 3, 0, 1, '2025-12-01 10:00:00'),

-- [应清理] 因子：逻辑删除(1) + 状态强制(3) + 时间过期
(2000000000000000002, 'test_a2@admin.com', 'A-强销-过期-待清', '$2a$10$pY9kwEJJB99zylONtPXgyeo8JFVjyOYk6LlZCkAmE2HpiRhNvFmQ6', 0, '2025-01-01', 3, 3, 1, '2025-11-01 10:00:00'),

-- [应清理] 因子：未逻删(0) + 状态强制(3) + 时间过期
(2000000000000000003, 'test_a3@admin.com', 'A-强销-未删-待清', '$2a$10$pY9kwEJJB99zylONtPXgyeo8JFVjyOYk6LlZCkAmE2HpiRhNvFmQ6', 0, '2025-01-01', 3, 3, 0, '2025-10-01 10:00:00'),

-- [保留] 因子：逻辑删除(1) + 状态警告(1) + 时间未过
(2000000000000000004, 'test_a4@admin.com', 'A-逻删-警告-保留', '$2a$10$pY9kwEJJB99zylONtPXgyeo8JFVjyOYk6LlZCkAmE2HpiRhNvFmQ6', 0, '2025-01-01', 3, 1, 1, '2026-04-01 10:00:00'),

-- [保留] 因子：未逻删(0) + 状态正常(0) + 时间最新
(2000000000000000005, 'test_a5@admin.com', 'A-正常-活跃-保留', '$2a$10$pY9kwEJJB99zylONtPXgyeo8JFVjyOYk6LlZCkAmE2HpiRhNvFmQ6', 2, '2025-01-01', 0, 0, 0, '2026-05-01 10:00:00');

-- 3. 关联头像测试数据 (t_account_avatar)
-- 策略：给 UID ...001 (应清理) 和 ...004 (保留) 添加头像，验证清理时是否同步删除
INSERT INTO `t_account_avatar` (`identity_type`, `uid`, `avatar_url`, `update_time`) VALUES
(1, 1000000000000000001, 'minio://bucket/avatars/user_1000000000000000001.jpg', '2025-12-01 10:00:00'),
(0, 2000000000000000001, 'minio://bucket/avatars/admin_2000000000000000001.jpg', '2025-12-01 10:00:00'),
(1, 1000000000000000004, 'minio://bucket/avatars/user_1000000000000000004.jpg', '2026-04-01 10:00:00');

-- 4. 关联异常状态测试数据 (t_account_exception_status_time)
-- 策略：给 UID ...002 (应清理-强销) 和 ...004 (保留-警告) 添加记录，验证联动清理
INSERT INTO `t_account_exception_status_time` (`uid`, `identity_type`, `exception_type`, `expire_time`, `reason`, `update_time`) VALUES
(1000000000000000002, 1, 3, '2025-12-01 10:00:00', '严重违规，永久封禁', '2025-11-01 10:00:00'),
(1000000000000000004, 1, 1, '2026-06-01 10:00:00', '轻微违规，警告一个月', '2026-04-01 10:00:00'),
(2000000000000000002, 0, 3, '2025-12-01 10:00:00', '管理失职，强制销号', '2025-11-01 10:00:00');








