package com.comonon.mall.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RefundApplyRequest {
    @NotBlank(message = "请填写退款原因")
    @Size(max = 256)
    private String reason;
}
