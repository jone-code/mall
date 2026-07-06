package com.comonon.mall.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class WechatLoginRequest {
    @NotBlank
    private String code;

    private String deviceId;

    @NotBlank
    @Pattern(regexp = "^(APP_IOS|APP_ANDROID|H5|MP_WX)$")
    private String deviceType;
}
