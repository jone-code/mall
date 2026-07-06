package com.comonon.mall.product.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpuSummaryVO {
    private Long id;
    private String title;
    private String subtitle;
    private String mainImage;
    private String productType;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
