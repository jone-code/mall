-- 订单 + 支付 + 库存锁定表（Phase 3/4 增量执行）
-- 用法: mysql --default-character-set=utf8mb4 -h127.0.0.1 -P13306 -uroot -proot mall < patch-order-pay-tables.sql
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE mall;

-- ===================================================================
-- 库存锁定（product-service，配合 sku_stock/stock_flow）
-- ===================================================================
CREATE TABLE IF NOT EXISTS `stock_lock` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT,
  `order_no`   VARCHAR(32) NOT NULL,
  `sku_id`     BIGINT      NOT NULL,
  `quantity`   INT         NOT NULL,
  `status`     TINYINT     NOT NULL DEFAULT 1,   -- 1 LOCKED / 2 RELEASED / 3 DEDUCTED
  `expire_at`  DATETIME    NOT NULL,
  `created_at` DATETIME    NOT NULL,
  `updated_at` DATETIME    NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_sku` (`order_no`, `sku_id`),
  KEY `idx_order` (`order_no`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单库存锁定';

-- ===================================================================
-- 订单（order-service）
-- ===================================================================
CREATE TABLE IF NOT EXISTS `orders` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT,
  `order_no`       VARCHAR(32)   NOT NULL,
  `user_id`        BIGINT        NOT NULL,
  `status`         VARCHAR(16)   NOT NULL,
  `total_amount`   DECIMAL(12,2) NOT NULL,
  `freight_amount` DECIMAL(12,2) NOT NULL DEFAULT 0,
  `pay_amount`     DECIMAL(12,2) NOT NULL,
  `item_count`     INT           NOT NULL,
  `product_type`   VARCHAR(16)   NOT NULL,
  `receiver`       VARCHAR(32),
  `receiver_phone` VARCHAR(20),
  `address_detail` VARCHAR(512),
  `expire_at`      DATETIME      NOT NULL,
  `pay_at`         DATETIME,
  `cancel_at`      DATETIME,
  `cancel_reason`  VARCHAR(64),
  `remark`         VARCHAR(255),
  `created_at`     DATETIME      NOT NULL,
  `updated_at`     DATETIME      NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_status_expire` (`status`, `expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单主表';

CREATE TABLE IF NOT EXISTS `order_item` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT,
  `order_no`     VARCHAR(32)   NOT NULL,
  `sku_id`       BIGINT        NOT NULL,
  `spu_id`       BIGINT        NOT NULL,
  `title`        VARCHAR(128)  NOT NULL,
  `spec_text`    VARCHAR(256),
  `main_image`   VARCHAR(512),
  `price`        DECIMAL(10,2) NOT NULL,
  `quantity`     INT           NOT NULL,
  `subtotal`     DECIMAL(12,2) NOT NULL,
  `product_type` VARCHAR(16)   NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_sku` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单商品行';

CREATE TABLE IF NOT EXISTS `order_log` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `order_no`      VARCHAR(32)  NOT NULL,
  `from_status`   VARCHAR(16),
  `to_status`     VARCHAR(16)  NOT NULL,
  `operator_type` VARCHAR(16)  NOT NULL,
  `operator_id`   VARCHAR(64),
  `remark`        VARCHAR(255),
  `created_at`    DATETIME     NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单状态流转日志';

-- ===================================================================
-- 支付（pay-service）
-- ===================================================================
CREATE TABLE IF NOT EXISTS `payment` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `pay_no`      VARCHAR(32)   NOT NULL,
  `order_no`    VARCHAR(32)   NOT NULL,
  `user_id`     BIGINT        NOT NULL,
  `channel`     VARCHAR(16)   NOT NULL,
  `amount`      DECIMAL(12,2) NOT NULL,
  `status`      VARCHAR(16)   NOT NULL,
  `channel_txn` VARCHAR(64),
  `paid_at`     DATETIME,
  `created_at`  DATETIME      NOT NULL,
  `updated_at`  DATETIME      NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pay_no` (`pay_no`),
  KEY `idx_order` (`order_no`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付单';

-- ===================================================================
-- 后台订单写权限
-- ===================================================================
INSERT IGNORE INTO `admin_permission` (`id`,`code`,`name`,`module`) VALUES (15,'order:write','订单运营操作','order');
INSERT IGNORE INTO `admin_role_permission` (`role_id`,`permission_id`)
SELECT 1, p.id FROM admin_permission p
WHERE p.code='order:write'
  AND NOT EXISTS (SELECT 1 FROM admin_role_permission rp WHERE rp.role_id=1 AND rp.permission_id=p.id);
