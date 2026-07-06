-- admin_audit_log 与实体对齐：补 username、error_msg；admin_user_id 允许为空（登录失败场景）
USE mall;

ALTER TABLE `admin_audit_log`
    ADD COLUMN `username` VARCHAR(64) NULL COMMENT '操作人登录名' AFTER `admin_user_id`;

ALTER TABLE `admin_audit_log`
    ADD COLUMN `error_msg` VARCHAR(512) NULL COMMENT '失败原因' AFTER `result`;

ALTER TABLE `admin_audit_log`
    MODIFY COLUMN `admin_user_id` BIGINT NULL;
