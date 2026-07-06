package com.comonon.mall.admin.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordLoginRequest {
    @NotBlank private String username;
    @NotBlank private String password;
    @NotBlank private String captchaId;
    @NotBlank private String captcha;
}
