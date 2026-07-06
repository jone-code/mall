package com.comonon.mall.order.vo;

import lombok.Data;

/** 订单履约信息（实物物流 / 虚拟卡密 / 服务核销码）。 */
@Data
public class FulfillmentVO {
    private String cardNo;
    private String cardSecret;
    private String verifyCode;
}
