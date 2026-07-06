-- 修复管理端种子中文乱码（双重 UTF-8 编码）
-- 用法: mysql --default-character-set=utf8mb4 -h127.0.0.1 -P13306 -uroot -proot mall < patch-utf8-admin-names.sql
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE mall;

UPDATE `admin_user` SET `real_name` = '超级管理员' WHERE `username` = 'admin';
UPDATE `admin_role` SET `name` = '超级管理员' WHERE `code` = 'SUPER_ADMIN';
UPDATE `admin_permission` SET `name` = '管理员查看' WHERE `code` = 'admin:user:read';
UPDATE `admin_permission` SET `name` = '管理员维护' WHERE `code` = 'admin:user:write';
