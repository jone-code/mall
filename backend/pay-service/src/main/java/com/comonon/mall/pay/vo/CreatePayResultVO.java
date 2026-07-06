package com.comonon.mall.pay.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePayResultVO {
    private String payNo;
    private String orderNo;
    private String channel;
    private BigDecimal amount;
    private String status;
    /** Mock 支付确认入口，前端按钮调用。 */
    private String mockConfirmUrl;
}
