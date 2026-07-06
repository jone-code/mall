package com.comonon.mall.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("admin_permission")
public class AdminPermission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private String module;
}
