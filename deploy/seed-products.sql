-- 商品域联调种子数据（可重复执行，会按 id 修正中文等字段）
-- 务必带字符集执行: mysql --default-character-set=utf8mb4 ... < seed-products.sql
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE mall;

INSERT INTO category (id, parent_id, name, icon_url, sort_order, status, level, created_at, updated_at) VALUES
(1, 0, '数码', NULL, 10, 1, 1, NOW(), NOW()),
(2, 0, '服饰', NULL, 20, 1, 1, NOW(), NOW()),
(11, 1, '手机', NULL, 10, 1, 2, NOW(), NOW()),
(12, 1, '电脑', NULL, 20, 1, 2, NOW(), NOW()),
(21, 2, '男装', NULL, 10, 1, 2, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), updated_at = NOW();

INSERT INTO spu (id, category_id, title, subtitle, product_type, main_image, images, detail_html, status, sort_order, created_at, updated_at) VALUES
(1001, 11, '透明防摔手机壳', '轻薄透明 防摔耐磨', 'PHYSICAL',
 'https://picsum.photos/seed/spu1001/400/400',
 '["https://picsum.photos/seed/spu1001a/400/400","https://picsum.photos/seed/spu1001b/400/400"]',
 '<p>适用于多款机型，TPU 软胶材质。</p>', 1, 100, NOW(), NOW()),
(1002, 21, '年度会员卡', '虚拟权益 即买即用', 'VIRTUAL',
 'https://picsum.photos/seed/spu1002/400/400', '[]',
 '<p>购买后自动发放卡密（Phase 5）。</p>', 1, 90, NOW(), NOW()),
(1003, 21, '上门量体服务', '专业裁缝上门', 'SERVICE',
 'https://picsum.photos/seed/spu1003/400/400', '[]',
 '<p>预约后客服联系（草稿状态）。</p>', 0, 80, NOW(), NOW())
ON DUPLICATE KEY UPDATE title = VALUES(title), subtitle = VALUES(subtitle),
  detail_html = VALUES(detail_html), status = VALUES(status), updated_at = NOW();

INSERT INTO sku (id, spu_id, sku_code, spec_json, spec_text, price, market_price, is_default, status, created_at, updated_at) VALUES
(2001, 1001, 'CASE-BLK-S', '{"dims":[{"name":"颜色","value":"黑色"},{"name":"型号","value":"标准款"}]}', '黑色 / 标准款', 29.90, 39.90, 1, 1, NOW(), NOW()),
(2002, 1001, 'CASE-RED-S', '{"dims":[{"name":"颜色","value":"红色"},{"name":"型号","value":"标准款"}]}', '红色 / 标准款', 29.90, 39.90, 0, 1, NOW(), NOW()),
(2003, 1001, 'CASE-BLK-P', '{"dims":[{"name":"颜色","value":"黑色"},{"name":"型号","value":"Pro款"}]}', '黑色 / Pro款', 39.90, 49.90, 0, 1, NOW(), NOW()),
(2004, 1002, 'VIP-YEAR', '{"dims":[{"name":"规格","value":"默认"}]}', '默认', 199.00, 299.00, 1, 1, NOW(), NOW()),
(2005, 1003, 'SVC-DEF', '{"dims":[{"name":"规格","value":"默认"}]}', '默认', 99.00, NULL, 1, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE spec_json = VALUES(spec_json), spec_text = VALUES(spec_text),
  price = VALUES(price), market_price = VALUES(market_price), updated_at = NOW();

INSERT INTO sku_stock (sku_id, available, frozen, version, updated_at) VALUES
(2001, 100, 0, 0, NOW()),
(2002, 50, 0, 0, NOW()),
(2003, 0, 0, 0, NOW()),
(2004, 999, 0, 0, NOW()),
(2005, 10, 0, 0, NOW())
ON DUPLICATE KEY UPDATE available = VALUES(available), updated_at = NOW();
