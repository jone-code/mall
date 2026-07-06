package com.comonon.mall.cart.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InternalCartLineVO {
    private Long skuId;
    private Integer quantity;
}
