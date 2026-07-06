package com.comonon.mall.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyServiceRequest {
    @NotBlank(message = "核销码不能为空")
    private String verifyCode;
}
