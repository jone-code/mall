package com.comonon.mall.order.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResultVO {
    private String orderNo;
    private BigDecimal payAmount;
    private LocalDateTime expireAt;
}
