-- 已有 mall 库缺 pwd_history 时执行（admin_user 表与实体对齐）
USE mall;

ALTER TABLE `admin_user`
    ADD COLUMN `pwd_history` TEXT NULL COMMENT '最近 N 次密码 BCrypt 哈希 JSON 数组'
    AFTER `pwd_updated_at`;

-- 默认管理员补历史记录（可选，便于密码策略校验）
UPDATE `admin_user`
SET `pwd_history` = CONCAT('["', `password`, '"]')
WHERE `username` = 'admin' AND (`pwd_history` IS NULL OR `pwd_history` = '');
