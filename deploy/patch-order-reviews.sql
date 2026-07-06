-- 订单评价表
-- mysql --default-character-set=utf8mb4 -h127.0.0.1 -P13306 -uroot -proot mall < patch-order-reviews.sql
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE mall;

CREATE TABLE IF NOT EXISTS `order_reviews` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(32) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `spu_id` BIGINT NOT NULL,
  `sku_id` BIGINT NULL,
  `rating` TINYINT NOT NULL COMMENT '1-5',
  `content` VARCHAR(1000) NOT NULL,
  `user_nickname` VARCHAR(64) NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'VISIBLE' COMMENT 'VISIBLE|HIDDEN',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
