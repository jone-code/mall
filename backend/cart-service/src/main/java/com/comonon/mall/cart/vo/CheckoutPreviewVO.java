package com.comonon.mall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class CheckoutPreviewVO {
    private List<CartItemVO> items = new ArrayList<>();
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private int itemCount;
    private int skuLineCount;
}
