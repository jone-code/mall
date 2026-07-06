package com.comonon.mall.product.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuDetailVO {
    private Long id;
    private String specText;
    private Object specJson;
    private BigDecimal price;
    private BigDecimal marketPrice;
    private Integer isDefault;
    private Integer available;
}
