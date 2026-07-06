package com.comonon.mall.common.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPrincipal {
    private Long userId;
    private String sid;
    private String deviceType;
    private String jti;
}
