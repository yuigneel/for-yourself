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
                                 `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知 3-强男 2-男 1-弱男 -1-弱女 -2-女 -3-强女',
                                 `birthday` DATE DEFAULT NULL COMMENT '出生日期',
    -- 🔥通用字段，不绑定业务
                                 `join_date` DATE NOT NULL COMMENT '平台入驻日期',
                                 `account_status` TINYINT NOT NULL DEFAULT 0 COMMENT '账户状态：3-强制销号 2-禁用 1-警告 0-正常',
                                 `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
                                 `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间(自动生成)',
                                 `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间(自动更新)',
                                 `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '最后更新人ID',

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
                                `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知 3-强男 2-男 1-弱男 -1-弱女 -2-女 -3-强女',
                                `birthday` DATE DEFAULT NULL COMMENT '出生日期',
                                `join_date` DATE NOT NULL COMMENT '平台入驻日期',
                                `account_permission` TINYINT NOT NULL DEFAULT 3 COMMENT '账户权限：0-最高权限 数值越大权限越低 当前最低为3',
                                `account_status` TINYINT NOT NULL DEFAULT 0 COMMENT '账户状态：3-强制销号 2-禁用 1-警告 0-正常',
                                `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
                                `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间(自动生成)',
                                `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间(自动更新)',
                                `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '最后更新人ID',

    -- 索引
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_uid` (`uid`),
                                UNIQUE KEY `uk_email` (`email`),
                                UNIQUE KEY `uk_nickname` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员用户基础信息表';

-- 插入初始管理员账号（最高权限）
INSERT INTO `t_admin_user` (`uid`, `email`, `nickname`, `password`, `join_date`, `account_permission`)
VALUES (2043953017778077696, 'yu_lgnier@outlook.com', '小白', '$2a$10$pY9kwEJJB99zylONtPXgyeo8JFVjyOYk6LlZCkAmE2HpiRhNvFmQ6', CURDATE(), 0);










