package com.comonon.mall.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    /** 当前会话标识；客户端在登录响应里拿到 */
    @NotBlank
    private String sid;

    @NotBlank
    private String refreshToken;
}
