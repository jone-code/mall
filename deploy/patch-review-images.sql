-- 评价晒图字段（Mock 图片 URL 存 JSON 数组）
-- mysql --default-character-set=utf8mb4 -h127.0.0.1 -P13306 -uroot -proot mall < patch-review-images.sql
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE mall;

ALTER TABLE `order_reviews`
  ADD COLUMN `images` JSON NULL COMMENT '晒图 URL 列表' AFTER `content`;
