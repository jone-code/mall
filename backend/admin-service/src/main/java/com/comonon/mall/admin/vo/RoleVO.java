package com.comonon.mall.admin.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoleVO {
    private Long id;
    private String code;
    private String name;
    private String remark;
    private int permissionCount;
    private List<Long> permissionIds;
}
