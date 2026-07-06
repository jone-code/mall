-- Meko 风格演示商品扩充（可重复执行）
-- mysql --default-character-set=utf8mb4 -h127.0.0.1 -P13306 -uroot -proot mall < seed-products-meko-demo.sql
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE mall;

INSERT INTO category (id, parent_id, name, icon_url, sort_order, status, level, created_at, updated_at) VALUES
(3, 0, '家居', NULL, 30, 1, 1, NOW(), NOW()),
(31, 3, '香氛', NULL, 10, 1, 2, NOW(), NOW()),
(22, 2, '女装', NULL, 20, 1, 2, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), sort_order = VALUES(sort_order), updated_at = NOW();

INSERT INTO spu (id, category_id, title, subtitle, product_type, main_image, images, detail_html, status, sort_order, created_at, updated_at) VALUES
(1003, 21, '专业裁缝上门量体', '预约上门 · 一对一服务', 'SERVICE',
 'https://picsum.photos/seed/comonon-svc/600/800', '[]',
 '<p>资深裁缝上门量体，支持西装与大衣定制咨询。</p>', 1, 95, NOW(), NOW()),
(1004, 21, '亚麻休闲衬衫', 'Breathable · 四季可穿', 'PHYSICAL',
 'https://picsum.photos/seed/comonon-shirt/600/800',
 '["https://picsum.photos/seed/comonon-shirt-a/600/800","https://picsum.photos/seed/comonon-shirt-b/600/800"]',
 '<p>100% 亚麻，自然褶皱质感，Meko 极简衣橱必备。</p>', 1, 120, NOW(), NOW()),
(1005, 21, '羊毛双面大衣', 'Classic fit · 秋冬系列', 'PHYSICAL',
 'https://picsum.photos/seed/comonon-coat/600/800', '[]',
 '<p>双面羊毛，挺括廓形，内部全衬里。</p>', 1, 115, NOW(), NOW()),
(1006, 21, '轻量缓震跑鞋', 'Urban runner · 日常通勤', 'PHYSICAL',
 'https://picsum.photos/seed/comonon-shoe/600/800', '[]',
 '<p>轻量中底，透气网面，适合城市步行与慢跑。</p>', 1, 110, NOW(), NOW()),
(1007, 22, '迷你皮革托特包', 'Handcrafted · 限量色', 'PHYSICAL',
 'https://picsum.photos/seed/comonon-bag/600/800', '[]',
 '<p>头层牛皮，磁吸扣，可斜挎。</p>', 1, 108, NOW(), NOW()),
(1008, 11, '无线降噪耳机', 'ANC · 30h 续航', 'PHYSICAL',
 'https://picsum.photos/seed/comonon-earphone/600/800', '[]',
 '<p>主动降噪，支持多设备切换。</p>', 1, 105, NOW(), NOW()),
(1009, 12, '矮轴机械键盘', '静音红轴 · 75% 配列', 'PHYSICAL',
 'https://picsum.photos/seed/comonon-keyboard/600/800', '[]',
 '<p>PBT 键帽，三模连接。</p>', 1, 102, NOW(), NOW()),
(1010, 31, '大豆蜡香薰', 'Sandalwood · 可持续', 'PHYSICAL',
 'https://picsum.photos/seed/comonon-candle/600/800', '[]',
 '<p>天然大豆蜡，燃烧约 45 小时。</p>', 1, 98, NOW(), NOW()),
(1011, 22, '有机棉基础 T 恤', 'Essential · 多色可选', 'PHYSICAL',
 'https://picsum.photos/seed/comonon-tee/600/800', '[]',
 '<p>GOTS 有机棉，修身版型。</p>', 1, 92, NOW(), NOW()),
(1012, 21, '数字壁纸合集', '4K · 即时下载', 'VIRTUAL',
 'https://picsum.photos/seed/comonon-wallpaper/600/800', '[]',
 '<p>购买后自动发放下载链接。</p>', 1, 88, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  category_id = VALUES(category_id),
  title = VALUES(title),
  subtitle = VALUES(subtitle),
  main_image = VALUES(main_image),
  images = VALUES(images),
  detail_html = VALUES(detail_html),
  status = VALUES(status),
  sort_order = VALUES(sort_order),
  updated_at = NOW();

UPDATE spu SET
  main_image = 'https://picsum.photos/seed/comonon-case/600/800',
  images = '["https://picsum.photos/seed/comonon-case-a/600/800","https://picsum.photos/seed/comonon-case-b/600/800"]',
  subtitle = 'Clear case · Drop tested',
  sort_order = 100,
  status = 1
WHERE id = 1001;

UPDATE spu SET
  main_image = 'https://picsum.photos/seed/comonon-vip/600/800',
  subtitle = 'Annual membership · Instant access',
  sort_order = 90,
  status = 1
WHERE id = 1002;

INSERT INTO sku (id, spu_id, sku_code, spec_json, spec_text, price, market_price, is_default, status, created_at, updated_at) VALUES
(2006, 1004, 'SHIRT-WHT-M', '{"dims":[{"name":"颜色","value":"本白"},{"name":"尺码","value":"M"}]}', '本白 / M', 359.00, 459.00, 1, 1, NOW(), NOW()),
(2007, 1004, 'SHIRT-BEG-L', '{"dims":[{"name":"颜色","value":"燕麦"},{"name":"尺码","value":"L"}]}', '燕麦 / L', 359.00, 459.00, 0, 1, NOW(), NOW()),
(2008, 1005, 'COAT-BLK-M', '{"dims":[{"name":"颜色","value":"炭黑"},{"name":"尺码","value":"M"}]}', '炭黑 / M', 1299.00, 1699.00, 1, 1, NOW(), NOW()),
(2009, 1006, 'SHOE-42', '{"dims":[{"name":"尺码","value":"42"}]}', '42码', 699.00, 899.00, 1, 1, NOW(), NOW()),
(2010, 1007, 'BAG-TAN', '{"dims":[{"name":"颜色","value":"焦糖"}]}', '焦糖', 899.00, 1199.00, 1, 1, NOW(), NOW()),
(2011, 1008, 'HP-BLK', '{"dims":[{"name":"颜色","value":"曜石黑"}]}', '曜石黑', 899.00, 1099.00, 1, 1, NOW(), NOW()),
(2012, 1009, 'KB-GRY', '{"dims":[{"name":"轴体","value":"静音红轴"}]}', '静音红轴', 599.00, 799.00, 1, 1, NOW(), NOW()),
(2013, 1010, 'CND-WD', '{"dims":[{"name":"香型","value":"檀木"}]}', '檀木', 168.00, 198.00, 1, 1, NOW(), NOW()),
(2014, 1011, 'TEE-WHT-S', '{"dims":[{"name":"颜色","value":"本白"},{"name":"尺码","value":"S"}]}', '本白 / S', 129.00, 159.00, 1, 1, NOW(), NOW()),
(2015, 1012, 'WP-STD', '{"dims":[{"name":"规格","value":"标准包"}]}', '标准包', 29.00, 49.00, 1, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE spec_text = VALUES(spec_text), price = VALUES(price),
  market_price = VALUES(market_price), updated_at = NOW();

INSERT INTO sku_stock (sku_id, available, frozen, version, updated_at) VALUES
(2006, 80, 0, 0, NOW()),
(2007, 60, 0, 0, NOW()),
(2008, 25, 0, 0, NOW()),
(2009, 120, 0, 0, NOW()),
(2010, 40, 0, 0, NOW()),
(2011, 200, 0, 0, NOW()),
(2012, 150, 0, 0, NOW()),
(2013, 300, 0, 0, NOW()),
(2014, 500, 0, 0, NOW()),
(2015, 9999, 0, 0, NOW())
ON DUPLICATE KEY UPDATE available = VALUES(available), updated_at = NOW();
