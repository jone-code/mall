-- 单库 mall：C 端 user 表 + 管理端 admin 表
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE DATABASE IF NOT EXISTS `mall` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `mall`;

-- C 端
CREATE TABLE IF NOT EXISTS `user` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `nickname`   VARCHAR(64)  NOT NULL,
    `avatar_url` VARCHAR(255),
    `status`     TINYINT      NOT NULL DEFAULT 0,
    `created_at` DATETIME     NOT NULL,
    `updated_at` DATETIME     NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `user_identity` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT       NOT NULL,
    `identity_type` VARCHAR(16)  NOT NULL,
    `identifier`    VARCHAR(128) NOT NULL,
    `union_id`      VARCHAR(128),
    `verified`      TINYINT      NOT NULL DEFAULT 1,
    `created_at`    DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_type_identifier` (`identity_type`, `identifier`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 管理后台
CREATE TABLE IF NOT EXISTS `admin_user` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `username`       VARCHAR(64)  NOT NULL,
    `password`       VARCHAR(255) NOT NULL,
    `real_name`      VARCHAR(64),
    `phone`          VARCHAR(20)  NOT NULL,
    `email`          VARCHAR(128),
    `status`         TINYINT      NOT NULL DEFAULT 0,
    `last_login_at`  DATETIME,
    `last_login_ip`  VARCHAR(45),
    `pwd_updated_at` DATETIME,
    `pwd_history`    TEXT         COMMENT '最近 N 次密码 BCrypt 哈希 JSON 数组',
    `created_at`     DATETIME     NOT NULL,
    `updated_at`     DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `admin_role` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `code`       VARCHAR(64)  NOT NULL,
    `name`       VARCHAR(64)  NOT NULL,
    `remark`     VARCHAR(255),
    `created_at` DATETIME     NOT NULL,
    `updated_at` DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `admin_permission` (
    `id`     BIGINT       NOT NULL AUTO_INCREMENT,
    `code`   VARCHAR(128) NOT NULL,
    `name`   VARCHAR(128) NOT NULL,
    `module` VARCHAR(64),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `admin_user_role` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT,
    `admin_user_id` BIGINT NOT NULL,
    `role_id`       BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`admin_user_id`, `role_id`),
    KEY `idx_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `admin_role_permission` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT,
    `role_id`       BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`),
    KEY `idx_perm` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `admin_audit_log` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `admin_user_id` BIGINT,
    `username`      VARCHAR(64),
    `action`        VARCHAR(64)  NOT NULL,
    `target_type`   VARCHAR(64),
    `target_id`     VARCHAR(64),
    `request_ip`    VARCHAR(45),
    `user_agent`    VARCHAR(255),
    `request_body`  TEXT,
    `result`        TINYINT      NOT NULL,
    `error_msg`     VARCHAR(512),
    `created_at`    DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_admin_time` (`admin_user_id`, `created_at`),
    KEY `idx_action` (`action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 默认管理员: admin / Admin@12345 (BCrypt cost=12)
INSERT IGNORE INTO `admin_user` (`id`,`username`,`password`,`real_name`,`phone`,`status`,`pwd_updated_at`,`pwd_history`,`created_at`,`updated_at`)
VALUES (1,'admin','$2a$12$ny2X4pDbFoKzTD35g.DeauVw2z5D1g6LNoaqhkSqh9138v5UNW7gG','超级管理员','13800000000',0,NOW(),'["$2a$12$ny2X4pDbFoKzTD35g.DeauVw2z5D1g6LNoaqhkSqh9138v5UNW7gG"]',NOW(),NOW());

INSERT IGNORE INTO `admin_role` (`id`,`code`,`name`,`created_at`,`updated_at`) VALUES (1,'SUPER_ADMIN','超级管理员',NOW(),NOW());
INSERT IGNORE INTO `admin_user_role` (`id`,`admin_user_id`,`role_id`) VALUES (1,1,1);
INSERT IGNORE INTO `admin_permission` (`id`,`code`,`name`,`module`) VALUES
  (1,'admin:user:read','管理员查看','admin'),
  (2,'admin:user:write','管理员维护','admin'),
  (3,'admin:user:create','创建管理员','admin'),
  (4,'admin:role:assign','分配管理员角色','admin'),
  (5,'admin:role:read','查看角色','admin'),
  (6,'admin:permission:read','查看权限','admin'),
  (7,'admin:permission:assign','分配角色权限','admin'),
  (8,'admin:audit:read','查看审计日志','admin'),
  (9,'member:read','查看会员用户','member'),
  (10,'member:write','维护会员用户','member'),
  (11,'product:read','查看商品','product'),
  (12,'order:read','查看订单','order'),
  (13,'card:read','查看虚拟卡','card'),
  (14,'product:write','维护商品','product'),
  (15,'order:write','订单运营操作','order'),
  (16,'admin:role:write','维护角色','admin'),
  (17,'admin:permission:write','维护权限点','admin'),
  (18,'card:import','导入虚拟卡密','card');
INSERT IGNORE INTO `admin_role_permission` (`id`,`role_id`,`permission_id`) VALUES
  (1,1,1),(2,1,2),(3,1,3),(4,1,4),(5,1,5),(6,1,6),(7,1,7),(8,1,8),(9,1,9),(10,1,10),(11,1,11),(12,1,12),(13,1,13),(14,1,14),(15,1,15),(16,1,16),(17,1,17),(18,1,18);
