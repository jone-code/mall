-- Mock 功能补全：退款、虚拟卡池、订单运营字段
-- mysql --default-character-set=utf8mb4 -h127.0.0.1 -P13306 -uroot -proot mall < patch-mock-features.sql
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE mall;

SET @db = DATABASE();

-- admin_remark
SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.columns
   WHERE table_schema = @db AND table_name = 'orders' AND column_name = 'admin_remark') = 0,
  'ALTER TABLE `orders` ADD COLUMN `admin_remark` VARCHAR(512) NULL AFTER `remark`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- refund_at
SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.columns
   WHERE table_schema = @db AND table_name = 'orders' AND column_name = 'refund_at') = 0,
  'ALTER TABLE `orders` ADD COLUMN `refund_at` DATETIME NULL AFTER `admin_remark`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- refund_reason
SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.columns
   WHERE table_schema = @db AND table_name = 'orders' AND column_name = 'refund_reason') = 0,
  'ALTER TABLE `orders` ADD COLUMN `refund_reason` VARCHAR(256) NULL AFTER `refund_at`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- refund_from_status
SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.columns
   WHERE table_schema = @db AND table_name = 'orders' AND column_name = 'refund_from_status') = 0,
  'ALTER TABLE `orders` ADD COLUMN `refund_from_status` VARCHAR(32) NULL AFTER `refund_reason`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- verified_at (服务核销)
SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.columns
   WHERE table_schema = @db AND table_name = 'orders' AND column_name = 'verified_at') = 0,
  'ALTER TABLE `orders` ADD COLUMN `verified_at` DATETIME NULL AFTER `fulfillment_json`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `virtual_cards` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `spu_id` BIGINT NULL COMMENT '可选绑定 SPU',
  `card_no` VARCHAR(64) NOT NULL,
  `card_secret` VARCHAR(128) NOT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE|ISSUED',
  `order_no` VARCHAR(32) NULL,
  `issued_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_card_no` (`card_no`),
  KEY `idx_status` (`status`),
  KEY `idx_spu` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
