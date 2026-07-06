package com.comonon.mall.pay.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderInfoDto {
    private String orderNo;
    private Long userId;
    private String status;
    private BigDecimal payAmount;
}
