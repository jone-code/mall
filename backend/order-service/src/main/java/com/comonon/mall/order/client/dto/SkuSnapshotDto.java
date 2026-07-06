package com.comonon.mall.order.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuSnapshotDto {
    private Long skuId;
    private Long spuId;
    private String specText;
    private BigDecimal price;
    private BigDecimal marketPrice;
    private Integer available;
    private Integer skuStatus;
    private Integer spuStatus;
    private String spuTitle;
    private String mainImage;
    private String productType;
    private Boolean sellable;
    private String invalidReason;
}
