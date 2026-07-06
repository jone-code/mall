package com.comonon.mall.product.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryTreeVO {
    private Long id;
    private Long parentId;
    private String name;
    private String iconUrl;
    private Integer sortOrder;
    private Integer status;
    private Integer level;
    private List<CategoryTreeVO> children = new ArrayList<>();
}
