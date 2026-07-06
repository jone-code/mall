package com.comonon.mall.product.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminSkuVO {
    private Long id;
    private Long spuId;
    private String skuCode;
    private Object specJson;
    private String specText;
    private BigDecimal price;
    private BigDecimal marketPrice;
    private Integer isDefault;
    private Integer status;
    private Integer available;
}
