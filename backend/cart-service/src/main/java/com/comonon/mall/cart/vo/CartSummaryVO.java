package com.comonon.mall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartSummaryVO {
    private int totalQuantity;
    private int selectedQuantity;
    private BigDecimal selectedAmount = BigDecimal.ZERO;
    private int invalidCount;
}
