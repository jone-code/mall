-- 支付单：同一订单同一状态只允许一条（防止并发创建多个 PENDING/SUCCESS）
-- 若索引已存在则跳过（重复执行安全）
SET @idx_exists := (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'payment'
      AND index_name = 'uk_order_status'
);
SET @sql := IF(@idx_exists = 0,
    'ALTER TABLE payment ADD UNIQUE KEY uk_order_status (order_no, status)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
