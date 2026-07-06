package com.comonon.mall.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCategoryRequest {
    private Long parentId;
    @NotBlank
    @Size(max = 64)
    private String name;
    private String iconUrl;
    private Integer sortOrder;
}
