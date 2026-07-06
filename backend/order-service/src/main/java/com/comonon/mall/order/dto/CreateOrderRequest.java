package com.comonon.mall.order.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateOrderRequest {

    /** 实物订单必填；虚拟/服务类可选。 */
    private Long addressId;

    @Size(max = 200)
    private String remark;
}
