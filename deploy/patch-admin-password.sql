-- 修复默认 admin 密码哈希（与 Admin@12345 对齐）
USE mall;

UPDATE `admin_user`
SET `password` = '$2a$12$ny2X4pDbFoKzTD35g.DeauVw2z5D1g6LNoaqhkSqh9138v5UNW7gG',
    `pwd_history` = '["$2a$12$ny2X4pDbFoKzTD35g.DeauVw2z5D1g6LNoaqhkSqh9138v5UNW7gG"]',
    `pwd_updated_at` = NOW(),
    `updated_at` = NOW()
WHERE `username` = 'admin';
