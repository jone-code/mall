-- 修复因 mysql 客户端未 SET NAMES utf8mb4 导致的双重 UTF-8 乱码
-- 用法: mysql --default-character-set=utf8mb4 -h127.0.0.1 -P13306 -uroot -proot mall < patch-utf8-fix-garbled.sql
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE mall;

UPDATE `admin_permission` SET `name` = '维护商品' WHERE `code` = 'product:write';
UPDATE `admin_permission` SET `name` = '订单运营操作' WHERE `code` = 'order:write';
