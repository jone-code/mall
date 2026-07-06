-- 补充管理后台权限（C端用户管理、权限管理、审计等）
-- 用法: mysql -h127.0.0.1 -P13306 -uroot -proot mall < mall/deploy/patch-admin-permissions.sql

SET NAMES utf8mb4;

INSERT IGNORE INTO `admin_permission` (`id`,`code`,`name`,`module`) VALUES
  (3,'admin:user:create','创建管理员','admin'),
  (4,'admin:role:assign','分配管理员角色','admin'),
  (5,'admin:role:read','查看角色','admin'),
  (6,'admin:permission:read','查看权限','admin'),
  (7,'admin:permission:assign','分配角色权限','admin'),
  (8,'admin:audit:read','查看审计日志','admin'),
  (9,'member:read','查看会员用户','member'),
  (10,'member:write','维护会员用户','member'),
  (11,'product:read','查看商品','product'),
  (12,'order:read','查看订单','order'),
  (13,'card:read','查看虚拟卡','card'),
  (14,'product:write','维护商品','product'),
  (15,'order:write','订单运营操作','order'),
  (18,'card:import','导入虚拟卡密','card');

INSERT IGNORE INTO `admin_role_permission` (`role_id`,`permission_id`)
SELECT 1, p.id FROM admin_permission p
WHERE p.id IN (1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,18)
  AND NOT EXISTS (
    SELECT 1 FROM admin_role_permission rp WHERE rp.role_id = 1 AND rp.permission_id = p.id
  );
