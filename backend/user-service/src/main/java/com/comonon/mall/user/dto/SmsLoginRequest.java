package com.comonon.mall.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SmsLoginRequest {
    @NotBlank
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须为 6 位数字")
    private String code;

    private String deviceId;

    @NotBlank
    @Pattern(regexp = "^(APP_IOS|APP_ANDROID|H5|MP_WX)$",
            message = "deviceType 必须为 APP_IOS/APP_ANDROID/H5/MP_WX")
    private String deviceType;
}
