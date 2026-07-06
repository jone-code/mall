-- 服务核销码池（须绑定 SERVICE 类型商品）
-- mysql --default-character-set=utf8mb4 -h127.0.0.1 -P13306 -uroot -proot mall < patch-service-verify-codes.sql
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE mall;

CREATE TABLE IF NOT EXISTS `service_verify_codes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `spu_id` BIGINT NOT NULL COMMENT '绑定的服务商品 SPU',
  `verify_code` VARCHAR(32) NOT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE|ISSUED',
  `order_no` VARCHAR(32) NULL,
  `issued_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_verify_code` (`verify_code`),
  KEY `idx_spu_status` (`spu_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
