package com.comonon.mall.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SmsSendRequest {
    @NotBlank
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    /** 场景，可选；默认 LOGIN */
    private String scene;
}
