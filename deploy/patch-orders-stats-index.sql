-- 订单统计索引 + 库存锁过期扫描索引（已有库增量执行）
-- 用法: mysql -h127.0.0.1 -P13306 -uroot -proot mall < mall/deploy/patch-orders-stats-index.sql

SET NAMES utf8mb4;

-- orders: Dashboard 今日订单/GMV 统计
SET @db := DATABASE();
SET @sql := IF(
  (SELECT COUNT(*) FROM information_schema.statistics
   WHERE table_schema = @db AND table_name = 'orders' AND index_name = 'idx_created_at') = 0,
  'ALTER TABLE `orders` ADD KEY `idx_created_at` (`created_at`)',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  (SELECT COUNT(*) FROM information_schema.statistics
   WHERE table_schema = @db AND table_name = 'orders' AND index_name = 'idx_paid') = 0,
  'ALTER TABLE `orders` ADD KEY `idx_paid` (`status`, `pay_at`)',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- stock_lock: 过期锁扫描
SET @sql := IF(
  (SELECT COUNT(*) FROM information_schema.statistics
   WHERE table_schema = @db AND table_name = 'stock_lock' AND index_name = 'idx_status_expire') = 0,
  'ALTER TABLE `stock_lock` ADD KEY `idx_status_expire` (`status`, `expire_at`)',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
