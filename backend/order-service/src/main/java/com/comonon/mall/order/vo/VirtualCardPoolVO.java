package com.comonon.mall.order.vo;

import lombok.Data;

@Data
public class VirtualCardPoolVO {
    private Long spuId;
    private long available;
    private long issued;
    private long total;
}
