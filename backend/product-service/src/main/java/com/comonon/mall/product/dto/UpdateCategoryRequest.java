package com.comonon.mall.product.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCategoryRequest {
    @Size(max = 64)
    private String name;
    private String iconUrl;
    private Integer sortOrder;
    private Integer status;
}
