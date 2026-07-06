package com.comonon.mall.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SessionVO {
    private String sid;
    private String deviceId;
    private String deviceType;
    private Long createdAt;
    private Long lastActiveAt;
    private boolean current;
}
