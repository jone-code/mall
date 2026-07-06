package com.comonon.mall.cart.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CartLine {
    private Long skuId;
    private Integer quantity;
    private Boolean selected;
    private LocalDateTime addedAt;
}
