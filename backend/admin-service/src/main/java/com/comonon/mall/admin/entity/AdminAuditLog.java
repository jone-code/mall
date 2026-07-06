package com.comonon.mall.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_audit_log")
public class AdminAuditLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long adminUserId;
    private String username;
    private String action;
    private String targetType;
    private String targetId;
    private String requestIp;
    private String userAgent;
    private String requestBody;
    /** 1 成功 0 失败 */
    private Integer result;
    private String errorMsg;
    private LocalDateTime createdAt;
}
