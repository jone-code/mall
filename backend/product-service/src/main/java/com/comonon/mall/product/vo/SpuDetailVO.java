package com.comonon.mall.product.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SpuDetailVO {
    private Long id;
    private String title;
    private String subtitle;
    private String productType;
    private String mainImage;
    private List<String> images = new ArrayList<>();
    private String detailHtml;
    private List<SkuDetailVO> skus = new ArrayList<>();
}
