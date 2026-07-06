-- 商品域表（已有库增量执行）
-- 用法: mysql --default-character-set=utf8mb4 -h127.0.0.1 -P13306 -uroot -proot mall < patch-product-tables.sql
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE mall;

CREATE TABLE IF NOT EXISTS `category` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `parent_id`  BIGINT       NOT NULL DEFAULT 0,
    `name`       VARCHAR(64)  NOT NULL,
    `icon_url`   VARCHAR(512),
    `sort_order` INT          NOT NULL DEFAULT 0,
    `status`     TINYINT      NOT NULL DEFAULT 1,
    `level`      TINYINT      NOT NULL DEFAULT 1,
    `created_at` DATETIME     NOT NULL,
    `updated_at` DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_parent` (`parent_id`),
    KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `spu` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT,
    `category_id`  BIGINT        NOT NULL,
    `title`        VARCHAR(128)  NOT NULL,
    `subtitle`     VARCHAR(256),
    `product_type` VARCHAR(16)   NOT NULL,
    `main_image`   VARCHAR(512)  NOT NULL,
    `images`       JSON,
    `detail_html`  MEDIUMTEXT,
    `status`       TINYINT       NOT NULL DEFAULT 0,
    `sort_order`   INT           NOT NULL DEFAULT 0,
    `created_at`   DATETIME      NOT NULL,
    `updated_at`   DATETIME      NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_category_status` (`category_id`, `status`),
    KEY `idx_type` (`product_type`),
    KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `sku` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT,
    `spu_id`       BIGINT        NOT NULL,
    `sku_code`     VARCHAR(64),
    `spec_json`    JSON          NOT NULL,
    `spec_text`    VARCHAR(256)  NOT NULL,
    `price`        DECIMAL(10,2) NOT NULL,
    `market_price` DECIMAL(10,2),
    `is_default`   TINYINT       NOT NULL DEFAULT 0,
    `status`       TINYINT       NOT NULL DEFAULT 1,
    `created_at`   DATETIME      NOT NULL,
    `updated_at`   DATETIME      NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_spu` (`spu_id`),
    UNIQUE KEY `uk_spu_spec` (`spu_id`, `spec_text`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `sku_stock` (
    `sku_id`     BIGINT   NOT NULL,
    `available`  INT      NOT NULL DEFAULT 0,
    `frozen`     INT      NOT NULL DEFAULT 0,
    `version`    INT      NOT NULL DEFAULT 0,
    `updated_at` DATETIME NOT NULL,
    PRIMARY KEY (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
