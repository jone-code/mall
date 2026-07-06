package com.comonon.mall.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShipOrderRequest {
    @NotBlank(message = "物流单号不能为空")
    private String trackingNo;

    private String trackingCompany;
}
