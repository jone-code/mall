-- ===================================================================
-- ComonOn Mall - schema.sql
-- 包含 C 端 user-service 与运营端 admin-service 表结构
-- 对应 auth-design.md §2 §11.2
-- 字符集 utf8mb4
-- ===================================================================

-- C 端用户表（库: mall）
CREATE TABLE IF NOT EXISTS `user` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `nickname`   VARCHAR(64)  NOT NULL                COMMENT '昵称',
    `avatar_url` VARCHAR(255)                          COMMENT '头像 URL',
    `status`     TINYINT      NOT NULL DEFAULT 0      COMMENT '0 正常 1 禁用 2 注销',
    `created_at` DATETIME     NOT NULL                COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL                COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户主表';

CREATE TABLE IF NOT EXISTS `user_identity` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT       NOT NULL                COMMENT '关联 user.id',
    `identity_type` VARCHAR(16)  NOT NULL                COMMENT 'PHONE / WECHAT',
    `identifier`    VARCHAR(128) NOT NULL                COMMENT '手机号 / openid',
    `union_id`      VARCHAR(128)                          COMMENT '微信 unionid',
    `verified`      TINYINT      NOT NULL DEFAULT 1      COMMENT '是否已验证',
    `created_at`    DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_type_identifier` (`identity_type`, `identifier`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户身份标识表';

-- ===================================================================
-- 管理后台表（库: mall，与 C 端同库）
-- ===================================================================

CREATE TABLE IF NOT EXISTS `admin_user` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `username`       VARCHAR(64)  NOT NULL                COMMENT '登录名',
    `password`       VARCHAR(255) NOT NULL                COMMENT 'BCrypt cost=12',
    `real_name`      VARCHAR(64)                           COMMENT '真实姓名',
    `phone`          VARCHAR(20)  NOT NULL                COMMENT '短信二步用',
    `email`          VARCHAR(128),
    `status`         TINYINT      NOT NULL DEFAULT 0      COMMENT '0 正常 1 停用',
    `last_login_at`  DATETIME,
    `last_login_ip`  VARCHAR(45),
    `pwd_updated_at` DATETIME                              COMMENT '密码更新时间',
    `pwd_history`    TEXT                                  COMMENT '最近 N 次密码 BCrypt 哈希 JSON 数组',
    `created_at`     DATETIME     NOT NULL,
    `updated_at`     DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='运营管理员账号';

CREATE TABLE IF NOT EXISTS `admin_role` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `code`       VARCHAR(64)  NOT NULL,
    `name`       VARCHAR(64)  NOT NULL,
    `remark`     VARCHAR(255),
    `created_at` DATETIME     NOT NULL,
    `updated_at` DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色';

CREATE TABLE IF NOT EXISTS `admin_permission` (
    `id`     BIGINT       NOT NULL AUTO_INCREMENT,
    `code`   VARCHAR(128) NOT NULL                COMMENT 'product:create / order:refund 等',
    `name`   VARCHAR(128) NOT NULL,
    `module` VARCHAR(64),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限点';

CREATE TABLE IF NOT EXISTS `admin_user_role` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT,
    `admin_user_id` BIGINT NOT NULL,
    `role_id`       BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`admin_user_id`, `role_id`),
    KEY `idx_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员-角色';

CREATE TABLE IF NOT EXISTS `admin_role_permission` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT,
    `role_id`       BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`),
    KEY `idx_perm` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色-权限';

CREATE TABLE IF NOT EXISTS `admin_audit_log` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `admin_user_id` BIGINT,
    `username`      VARCHAR(64)                           COMMENT '操作人登录名（登录失败时也可记录）',
    `action`        VARCHAR(64)  NOT NULL,
    `target_type`   VARCHAR(64),
    `target_id`     VARCHAR(64),
    `request_ip`    VARCHAR(45),
    `user_agent`    VARCHAR(255),
    `request_body`  TEXT,
    `result`        TINYINT      NOT NULL                COMMENT '1 成功 0 失败',
    `error_msg`     VARCHAR(512),
    `created_at`    DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_admin_time` (`admin_user_id`, `created_at`),
    KEY `idx_action` (`action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员审计日志';

-- ===================================================================
-- 商品域（product-service）
-- ===================================================================

CREATE TABLE IF NOT EXISTS `category` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `parent_id`  BIGINT       NOT NULL DEFAULT 0,
    `name`       VARCHAR(64)  NOT NULL,
    `icon_url`   VARCHAR(512),
    `sort_order` INT          NOT NULL DEFAULT 0,
    `status`     TINYINT      NOT NULL DEFAULT 1 COMMENT '0 禁用 1 启用',
    `level`      TINYINT      NOT NULL DEFAULT 1,
    `created_at` DATETIME     NOT NULL,
    `updated_at` DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_parent` (`parent_id`),
    KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品类目';

CREATE TABLE IF NOT EXISTS `spu` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT,
    `category_id`  BIGINT        NOT NULL,
    `title`        VARCHAR(128)  NOT NULL,
    `subtitle`     VARCHAR(256),
    `product_type` VARCHAR(16)   NOT NULL COMMENT 'PHYSICAL/VIRTUAL/SERVICE',
    `main_image`   VARCHAR(512)  NOT NULL,
    `images`       JSON,
    `detail_html`  MEDIUMTEXT,
    `status`       TINYINT       NOT NULL DEFAULT 0 COMMENT '0 草稿 1 上架 2 下架',
    `sort_order`   INT           NOT NULL DEFAULT 0,
    `created_at`   DATETIME      NOT NULL,
    `updated_at`   DATETIME      NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_category_status` (`category_id`, `status`),
    KEY `idx_type` (`product_type`),
    KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品 SPU';

CREATE TABLE IF NOT EXISTS `sku` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT,
    `spu_id`       BIGINT        NOT NULL,
    `sku_code`     VARCHAR(64),
    `spec_json`    JSON          NOT NULL,
    `spec_text`    VARCHAR(256)  NOT NULL,
    `price`        DECIMAL(10,2) NOT NULL,
    `market_price` DECIMAL(10,2),
    `is_default`   TINYINT       NOT NULL DEFAULT 0,
    `status`       TINYINT       NOT NULL DEFAULT 1 COMMENT '0 禁用 1 启用',
    `created_at`   DATETIME      NOT NULL,
    `updated_at`   DATETIME      NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_spu` (`spu_id`),
    UNIQUE KEY `uk_spu_spec` (`spu_id`, `spec_text`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品 SKU';

CREATE TABLE IF NOT EXISTS `sku_stock` (
    `sku_id`     BIGINT   NOT NULL,
    `available`  INT      NOT NULL DEFAULT 0,
    `frozen`     INT      NOT NULL DEFAULT 0,
    `version`    INT      NOT NULL DEFAULT 0,
    `updated_at` DATETIME NOT NULL,
    PRIMARY KEY (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SKU 库存';

CREATE TABLE IF NOT EXISTS `stock_flow` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `sku_id`          BIGINT       NOT NULL,
    `order_no`        VARCHAR(64),
    `change_type`     VARCHAR(32)  NOT NULL,
    `delta_available` INT          NOT NULL DEFAULT 0,
    `delta_frozen`    INT          NOT NULL DEFAULT 0,
    `available_after` INT          NOT NULL,
    `frozen_after`    INT          NOT NULL,
    `operator_type`   VARCHAR(16)  NOT NULL,
    `operator_id`     VARCHAR(64),
    `remark`          VARCHAR(255),
    `created_at`      DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_sku_time` (`sku_id`, `created_at`),
    KEY `idx_order` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存流水';

-- ===================================================================
-- 用户收货地址（user-service Phase 2）
-- ===================================================================

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
    `is_default`  TINYINT      NOT NULL DEFAULT 0 COMMENT '0 否 1 是',
    `created_at`  DATETIME     NOT NULL,
    `updated_at`  DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_user_default` (`user_id`, `is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收货地址';

-- ===================================================================
-- 订单库存锁定（product-service Phase 3）
-- ===================================================================
CREATE TABLE IF NOT EXISTS `stock_lock` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT,
  `order_no`   VARCHAR(32) NOT NULL,
  `sku_id`     BIGINT      NOT NULL,
  `quantity`   INT         NOT NULL,
  `status`     TINYINT     NOT NULL DEFAULT 1 COMMENT '1 LOCKED / 2 RELEASED / 3 DEDUCTED',
  `expire_at`  DATETIME    NOT NULL,
  `created_at` DATETIME    NOT NULL,
  `updated_at` DATETIME    NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_sku` (`order_no`, `sku_id`),
  KEY `idx_order` (`order_no`),
  KEY `idx_status` (`status`),
  KEY `idx_status_expire` (`status`, `expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单库存锁定';

-- ===================================================================
-- 订单（order-service Phase 3）
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
  KEY `idx_status_expire` (`status`, `expire_at`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_paid` (`status`, `pay_at`)
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
-- 支付（pay-service Phase 4）
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
  UNIQUE KEY `uk_order_status` (`order_no`, `status`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付单';
