package com.comonon.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentBriefVO {
    private String payNo;
    private String channel;
    private String status;
    private BigDecimal amount;
    private String channelTxn;
    private LocalDateTime paidAt;
}
