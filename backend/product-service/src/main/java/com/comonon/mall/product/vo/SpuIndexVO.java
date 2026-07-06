package com.comonon.mall.product.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpuIndexVO {
    private Long id;
    private String title;
    private String subtitle;
    private Long categoryId;
    private String productType;
    private String mainImage;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer sortOrder;
}
