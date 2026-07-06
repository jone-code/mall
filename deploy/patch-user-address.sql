-- 用户收货地址表（已有库增量执行）
-- 用法: mysql --default-character-set=utf8mb4 -h127.0.0.1 -P13306 -uroot -proot mall < patch-user-address.sql
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE mall;

CREATE TABLE IF NOT EXISTS `user_address` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT       NOT NULL,
    `receiver`    VARCHAR(32)  NOT NULL,
    `phone`       VARCHAR(20)  NOT NULL,
    `province`    VARCHAR(32)  NOT NULL,
    `city`        VARCHAR(32)  NOT NULL,
    `district`    VARCHAR(32)  NOT NULL,
    `detail`      VARCHAR(256) NOT NULL,
    `region_code` VARCHAR(12),
    `is_default`  TINYINT      NOT NULL DEFAULT 0,
    `created_at`  DATETIME     NOT NULL,
    `updated_at`  DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_user_default` (`user_id`, `is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
