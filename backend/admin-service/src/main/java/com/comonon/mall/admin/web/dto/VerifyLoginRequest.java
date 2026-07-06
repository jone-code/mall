package com.comonon.mall.admin.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyLoginRequest {
    @NotBlank private String challengeToken;
    @NotBlank private String smsCode;
}
