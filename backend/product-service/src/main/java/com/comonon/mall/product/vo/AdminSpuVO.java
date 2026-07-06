package com.comonon.mall.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class AdminSpuVO {
    private Long id;
    private Long categoryId;
    private String title;
    private String subtitle;
    private String productType;
    private String mainImage;
    private List<String> images = new ArrayList<>();
    private String detailHtml;
    private Integer status;
    private Integer sortOrder;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<AdminSkuVO> skus = new ArrayList<>();
}
