package com.comonon.mall.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateServiceVerifyCodeRequest {
    @NotNull(message = "请选择服务商品")
    private Long spuId;

    @NotBlank(message = "核销码不能为空")
    private String verifyCode;
}
