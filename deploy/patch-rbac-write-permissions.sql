-- 角色/权限点维护能力（已有库执行一次）
USE mall;

INSERT IGNORE INTO `admin_permission` (`id`, `code`, `name`, `module`) VALUES
  (16, 'admin:role:write', '维护角色', 'admin'),
  (17, 'admin:permission:write', '维护权限点', 'admin');

INSERT IGNORE INTO `admin_role_permission` (`role_id`, `permission_id`)
SELECT 1, p.id FROM `admin_permission` p WHERE p.code IN ('admin:role:write', 'admin:permission:write');
