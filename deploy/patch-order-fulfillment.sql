-- 订单履约字段：发货、完成、虚拟/服务交付信息
-- mysql --default-character-set=utf8mb4 -h127.0.0.1 -P13306 -uroot -proot mall < patch-order-fulfillment.sql
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE mall;

SET @db = DATABASE();

-- ship_at
SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.columns
   WHERE table_schema = @db AND table_name = 'orders' AND column_name = 'ship_at') = 0,
  'ALTER TABLE `orders` ADD COLUMN `ship_at` DATETIME NULL AFTER `pay_at`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- tracking_no
SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.columns
   WHERE table_schema = @db AND table_name = 'orders' AND column_name = 'tracking_no') = 0,
  'ALTER TABLE `orders` ADD COLUMN `tracking_no` VARCHAR(64) NULL AFTER `ship_at`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- tracking_company
SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.columns
   WHERE table_schema = @db AND table_name = 'orders' AND column_name = 'tracking_company') = 0,
  'ALTER TABLE `orders` ADD COLUMN `tracking_company` VARCHAR(32) NULL AFTER `tracking_no`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- complete_at
SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.columns
   WHERE table_schema = @db AND table_name = 'orders' AND column_name = 'complete_at') = 0,
  'ALTER TABLE `orders` ADD COLUMN `complete_at` DATETIME NULL AFTER `tracking_company`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- fulfillment_json
SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.columns
   WHERE table_schema = @db AND table_name = 'orders' AND column_name = 'fulfillment_json') = 0,
  'ALTER TABLE `orders` ADD COLUMN `fulfillment_json` TEXT NULL AFTER `complete_at`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
