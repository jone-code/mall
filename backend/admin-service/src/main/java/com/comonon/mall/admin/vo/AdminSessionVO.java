package com.comonon.mall.admin.vo;

import lombok.Data;

@Data
public class AdminSessionVO {
    private String sid;
    private Long createdAt;
    private Long lastActiveAt;
    private boolean current;
}
