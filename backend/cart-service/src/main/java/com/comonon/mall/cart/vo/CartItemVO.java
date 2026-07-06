package com.comonon.mall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartItemVO {
    private Long skuId;
    private Long spuId;
    private String title;
    private String specText;
    private String mainImage;
    private BigDecimal price;
    private Integer quantity;
    private Boolean selected;
    private Integer available;
    private Boolean sellable;
    private Boolean invalid;
    private String invalidReason;
    private Boolean stockInsufficient;
    private String productType;
    private LocalDateTime addedAt;
}
