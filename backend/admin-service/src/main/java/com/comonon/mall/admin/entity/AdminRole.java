package com.comonon.mall.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_role")
public class AdminRole {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
