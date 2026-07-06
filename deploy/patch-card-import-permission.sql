-- 虚拟卡密导入/新增权限（已有库执行一次即可）
-- mysql --default-character-set=utf8mb4 -h127.0.0.1 -P13306 -uroot -proot mall < patch-card-import-permission.sql
SET NAMES utf8mb4;
USE mall;

INSERT IGNORE INTO `admin_permission` (`id`,`code`,`name`,`module`) VALUES
  (18,'card:import','导入虚拟卡密','card');

INSERT IGNORE INTO `admin_role_permission` (`role_id`,`permission_id`)
SELECT 1, 18 FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM admin_role_permission WHERE role_id = 1 AND permission_id = 18
);
