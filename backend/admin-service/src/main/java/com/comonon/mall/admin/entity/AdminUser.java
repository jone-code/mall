package com.comonon.mall.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_user")
public class AdminUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    /** 0 正常 1 停用 */
    private Integer status;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private LocalDateTime pwdUpdatedAt;
    /** JSON array of recent password BCrypt hashes (most-recent first). */
    private String pwdHistory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
